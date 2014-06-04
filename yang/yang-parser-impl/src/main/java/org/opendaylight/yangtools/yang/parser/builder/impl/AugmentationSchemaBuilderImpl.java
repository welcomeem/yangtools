/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NamespaceRevisionAware;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class AugmentationSchemaBuilderImpl extends AbstractDataNodeContainerBuilder implements
        AugmentationSchemaBuilder {
    private final int order;
    private AugmentationSchemaImpl instance;
    private String whenCondition;

    private String description;
    private String reference;
    private Status status = Status.CURRENT;

    private final String augmentTargetStr;
    private final SchemaPath targetPath;
    private SchemaPath targetNodeSchemaPath;

    private boolean resolved;
    private AugmentationSchemaBuilder copyOf;

    public AugmentationSchemaBuilderImpl(final String moduleName, final int line, final String augmentTargetStr, final int order) {
        super(moduleName, line, null);
        this.order = order;
        this.augmentTargetStr = augmentTargetStr;
        targetPath = BuilderUtils.parseXPathString(augmentTargetStr);
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return Collections.emptySet();
    }

    @Override
    public Set<GroupingBuilder> getGroupingBuilders() {
        return Collections.emptySet();
    }

    @Override
    public void addGrouping(final GroupingBuilder grouping) {
        throw new YangParseException(getModuleName(), grouping.getLine(), "augment can not contains grouping statement");
    }

    @Override
    public SchemaPath getPath() {
        return targetNodeSchemaPath;
    }

    @Override
    public AugmentationSchema build() {
        if (instance != null) {
            return instance;
        }

        instance = new AugmentationSchemaImpl(targetPath, order);

        instance.description = description;
        instance.reference = reference;
        instance.status = status;

        Builder parent = getParent();
        if (parent instanceof ModuleBuilder) {
            ModuleBuilder moduleBuilder = (ModuleBuilder) parent;
            instance.namespace = moduleBuilder.getNamespace();
            instance.revision = moduleBuilder.getRevision();
        }

        if (parent instanceof UsesNodeBuilder) {
            final ModuleBuilder mb = BuilderUtils.getParentModule(this);
            final QNameModule qm = QNameModule.create(mb.getNamespace(), mb.getRevision());

            List<QName> newPath = new ArrayList<>();
            for (QName name : targetPath.getPathFromRoot()) {
                newPath.add(QName.create(qm, name.getPrefix(), name.getLocalName()));
            }
            instance.targetPath = SchemaPath.create(newPath, false);
        } else {
            instance.targetPath = targetNodeSchemaPath;
        }

        if (copyOf != null) {
            instance.setCopyOf(copyOf.build());
        }

        RevisionAwareXPath whenStmt;
        if (whenCondition == null) {
            whenStmt = null;
        } else {
            whenStmt = new RevisionAwareXPathImpl(whenCondition, false);
        }
        instance.whenCondition = whenStmt;

        // CHILD NODES
        for (DataSchemaNodeBuilder node : addedChildNodes) {
            childNodes.put(node.getQName(), node.build());
        }
        instance.childNodes = ImmutableSet.copyOf(childNodes.values());

        // USES
        for (UsesNodeBuilder builder : addedUsesNodes) {
            usesNodes.add(builder.build());
        }
        instance.uses = ImmutableSet.copyOf(usesNodes);

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    @Override
    public boolean isResolved() {
        return resolved;
    }

    @Override
    public void setResolved(final boolean resolved) {
        this.resolved = resolved;
    }

    @Override
    public String getWhenCondition() {
        return whenCondition;
    }

    @Override
    public void addWhenCondition(final String whenCondition) {
        this.whenCondition = whenCondition;
    }

    @Override
    public Set<TypeDefinitionBuilder> getTypeDefinitionBuilders() {
        return Collections.emptySet();
    }

    @Override
    public void addTypedef(final TypeDefinitionBuilder type) {
        throw new YangParseException(getModuleName(), type.getLine(), "Augmentation can not contains typedef statement.");
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(final Status status) {
        this.status = Preconditions.checkNotNull(status, "status cannot be null");
    }

    @Override
    public String getTargetPathAsString() {
        return augmentTargetStr;
    }

    @Override
    public SchemaPath getTargetPath() {
        return targetPath;
    }

    @Override
    public SchemaPath getTargetNodeSchemaPath() {
        return targetNodeSchemaPath;
    }

    @Override
    public void setTargetNodeSchemaPath(final SchemaPath path) {
        this.targetNodeSchemaPath = path;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + ((augmentTargetStr == null) ? 0 : augmentTargetStr.hashCode());
        result = prime * result + ((whenCondition == null) ? 0 : whenCondition.hashCode());
        result = prime * result + ((addedChildNodes == null) ? 0 : addedChildNodes.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AugmentationSchemaBuilderImpl other = (AugmentationSchemaBuilderImpl) obj;
        if (augmentTargetStr == null) {
            if (other.augmentTargetStr != null) {
                return false;
            }
        } else if (!augmentTargetStr.equals(other.augmentTargetStr)) {
            return false;
        }
        if (whenCondition == null) {
            if (other.whenCondition != null) {
                return false;
            }
        } else if (!whenCondition.equals(other.whenCondition)) {
            return false;
        }
        if (addedChildNodes == null) {
            if (other.addedChildNodes != null) {
                return false;
            }
        } else if (!addedChildNodes.equals(other.addedChildNodes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "augment " + augmentTargetStr;
    }

    public void setCopyOf(final AugmentationSchemaBuilder old) {
        copyOf = old;
    }

    private static final class AugmentationSchemaImpl implements AugmentationSchema, NamespaceRevisionAware,
            Comparable<AugmentationSchemaImpl> {
        private final int order;
        private SchemaPath targetPath;
        private RevisionAwareXPath whenCondition;
        private ImmutableSet<DataSchemaNode> childNodes;
        private ImmutableSet<UsesNode> uses;
        private String description;
        private String reference;
        private Status status;

        private URI namespace;
        private Date revision;
        private ImmutableList<UnknownSchemaNode> unknownNodes;
        private AugmentationSchema copyOf;

        public AugmentationSchemaImpl(final SchemaPath targetPath, final int order) {
            this.targetPath = targetPath;
            this.order = order;
        }

        public void setCopyOf(final AugmentationSchema build) {
            this.copyOf = build;
        }

        @Override
        public Optional<AugmentationSchema> getOriginalDefinition() {
            return Optional.fromNullable(this.copyOf);
        }

        @Override
        public SchemaPath getTargetPath() {
            return targetPath;
        }

        @Override
        public RevisionAwareXPath getWhenCondition() {
            return whenCondition;
        }

        @Override
        public Set<DataSchemaNode> getChildNodes() {
            return childNodes;
        }

        /**
         * Always returns an empty set, because augment can not contains
         * grouping statement.
         */
        @Override
        public Set<GroupingDefinition> getGroupings() {
            return Collections.emptySet();
        }

        @Override
        public Set<UsesNode> getUses() {
            return uses;
        }

        /**
         * Always returns an empty set, because augment can not contains type
         * definitions.
         */
        @Override
        public Set<TypeDefinition<?>> getTypeDefinitions() {
            return Collections.emptySet();
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        @Override
        public DataSchemaNode getDataChildByName(final QName name) {
            return getChildNode(childNodes, name);
        }

        @Override
        public DataSchemaNode getDataChildByName(final String name) {
            return getChildNode(childNodes, name);
        }

        @Override
        public URI getNamespace() {
            return namespace;
        }

        @Override
        public Date getRevision() {
            return revision;
        }

        @Override
        public int hashCode() {
            final int prime = 17;
            int result = 1;
            result = prime * result + ((targetPath == null) ? 0 : targetPath.hashCode());
            result = prime * result + ((whenCondition == null) ? 0 : whenCondition.hashCode());
            result = prime * result + ((childNodes == null) ? 0 : childNodes.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AugmentationSchemaImpl other = (AugmentationSchemaImpl) obj;
            if (targetPath == null) {
                if (other.targetPath != null) {
                    return false;
                }
            } else if (!targetPath.equals(other.targetPath)) {
                return false;
            }
            if (whenCondition == null) {
                if (other.whenCondition != null) {
                    return false;
                }
            } else if (!whenCondition.equals(other.whenCondition)) {
                return false;
            }
            if (childNodes == null) {
                if (other.childNodes != null) {
                    return false;
                }
            } else if (!childNodes.equals(other.childNodes)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(AugmentationSchemaImpl.class.getSimpleName());
            sb.append("[");
            sb.append("targetPath=" + targetPath);
            sb.append(", when=" + whenCondition);
            sb.append("]");
            return sb.toString();
        }

        @Override
        public int compareTo(final AugmentationSchemaImpl o) {
            Iterator<QName> thisIt = this.targetPath.getPathFromRoot().iterator();
            Iterator<QName> otherIt = o.getTargetPath().getPathFromRoot().iterator();
            while (thisIt.hasNext()) {
                if (otherIt.hasNext()) {
                    int comp = thisIt.next().compareTo(otherIt.next());
                    if (comp != 0) {
                        return comp;
                    }
                } else {
                    return 1;
                }
            }
            if (otherIt.hasNext()) {
                return -1;
            }
            return this.order - o.order;
        }
    }

}
