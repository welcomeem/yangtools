/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

final class CaseShorthandImpl implements ChoiceCaseNode, DerivableSchemaNode {

    private final DataSchemaNode caseShorthandNode;
    private final ChoiceCaseNode original;
    private final SchemaPath path;
    private final boolean augmenting;

    CaseShorthandImpl(final DataSchemaNode caseShorthandNode) {
        this.caseShorthandNode = Preconditions.checkNotNull(caseShorthandNode);
        this.path = Preconditions.checkNotNull(caseShorthandNode.getPath().getParent());
        this.original = getOriginalIfPresent(caseShorthandNode);

        // We need to cache this, as it will be reset
        this.augmenting = caseShorthandNode.isAugmenting();
    }

    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return caseShorthandNode.isAddedByUses();
    }

    @Override
    public boolean isConfiguration() {
        return caseShorthandNode.isConfiguration();
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return caseShorthandNode.getConstraints();
    }

    @Override
    public QName getQName() {
        return caseShorthandNode.getQName();
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return ImmutableList.of();
    }

    @Override
    public String getDescription() {
        return caseShorthandNode.getDescription();
    }

    @Override
    public String getReference() {
        return caseShorthandNode.getReference();
    }

    @Override
    public Status getStatus() {
        return caseShorthandNode.getStatus();
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return ImmutableSet.of();
    }

    @Override
    public Collection<DataSchemaNode> getChildNodes() {
        return ImmutableList.of(caseShorthandNode);
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return ImmutableSet.of();
    }

    @Override
    public DataSchemaNode getDataChildByName(final QName name) {
        if (getQName().equals(name)) {
            return caseShorthandNode;
        } else {
            return null;
        }
    }

    @Override
    public DataSchemaNode getDataChildByName(final String name) {
        if (getQName().getLocalName().equals(name)) {
            return caseShorthandNode;
        } else {
            return null;
        }
    }

    @Override
    public Set<UsesNode> getUses() {
        return ImmutableSet.of();
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return ImmutableSet.of();
    }

    @Override
    public Optional<? extends SchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getQName());
        result = prime * result + Objects.hashCode(path);
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
        CaseShorthandImpl other = (CaseShorthandImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(path, other.path);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(CaseShorthandImpl.class.getSimpleName());
        sb.append("[");
        sb.append("qname=");
        sb.append(getQName());
        sb.append("]");
        return sb.toString();
    }

    private static ChoiceCaseNode getOriginalIfPresent(final SchemaNode caseShorthandNode) {
        if (caseShorthandNode instanceof DerivableSchemaNode) {
            final Optional<? extends SchemaNode> original = ((DerivableSchemaNode) caseShorthandNode).getOriginal();
            if (original.isPresent()) {
                return new CaseShorthandImpl((DataSchemaNode) original.get());
            }
        }
        return null;
    }
}
