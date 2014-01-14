/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;

final class SchemaContextImpl implements SchemaContext {
    private final Set<Module> modules;

    SchemaContextImpl(final Set<Module> modules) {
        this.modules = modules;
    }

    @Override
    public Set<DataSchemaNode> getDataDefinitions() {
        final Set<DataSchemaNode> dataDefs = new HashSet<DataSchemaNode>();
        for (Module m : modules) {
            dataDefs.addAll(m.getChildNodes());
        }
        return dataDefs;
    }

    @Override
    public Set<Module> getModules() {
        List<Module> sorted = ModuleDependencySort.sort(modules.toArray(new Module[modules.size()]));
        return new LinkedHashSet<Module>(sorted);
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        final Set<NotificationDefinition> notifications = new HashSet<NotificationDefinition>();
        for (Module m : modules) {
            notifications.addAll(m.getNotifications());
        }
        return notifications;
    }

    @Override
    public Set<RpcDefinition> getOperations() {
        final Set<RpcDefinition> rpcs = new HashSet<RpcDefinition>();
        for (Module m : modules) {
            rpcs.addAll(m.getRpcs());
        }
        return rpcs;
    }

    @Override
    public Set<ExtensionDefinition> getExtensions() {
        final Set<ExtensionDefinition> extensions = new HashSet<ExtensionDefinition>();
        for (Module m : modules) {
            extensions.addAll(m.getExtensionSchemaNodes());
        }
        return extensions;
    }

    @Override
    public Module findModuleByName(final String name, final Date revision) {
        if (name != null) {
            for (final Module module : modules) {
                if (revision == null) {
                    if (module.getName().equals(name)) {
                        return module;
                    }
                } else if (module.getName().equals(name) && module.getRevision().equals(revision)) {
                    return module;
                }
            }
        }
        return null;
    }

    @Override
    public Set<Module> findModuleByNamespace(final URI namespace) {
        final Set<Module> ret = new HashSet<Module>();
        if (namespace != null) {
            for (final Module module : modules) {
                if (module.getNamespace().equals(namespace)) {
                    ret.add(module);
                }
            }
        }
        return ret;
    }

    @Override
    public Module findModuleByNamespaceAndRevision(URI namespace, Date revision) {
        if (namespace != null) {
            for (final Module module : modules) {
                if (module.getNamespace().equals(namespace) && module.getRevision().equals(revision)) {
                    return(module);
                }
            }
        }
        return null;
    }

}
