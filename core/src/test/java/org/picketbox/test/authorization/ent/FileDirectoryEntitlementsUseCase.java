/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketbox.test.authorization.ent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.picketbox.core.UserContext;
import org.picketbox.core.authorization.Resource;
import org.picketbox.core.authorization.ent.Entitlement;
import org.picketbox.core.authorization.ent.EntitlementCollection;
import org.picketbox.core.authorization.ent.EntitlementStore;
import org.picketbox.core.authorization.ent.EntitlementsManager;
import org.picketbox.core.authorization.ent.impl.DefaultEntitlementsManager;
import org.picketbox.core.authorization.ent.impl.SimpleEntitlement;
import org.picketbox.core.authorization.impl.SimpleResource;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

/**
 * Unit test the {@link EntitlementsManager} using the classic Unix File Permissions use case
 *
 * @author anil saldhana
 * @since Oct 25, 2012
 */
public class FileDirectoryEntitlementsUseCase {
    private EntitlementsManager mgr = null;

    private User useranil = new SimpleUser("anil");
    private Role employee = new SimpleRole("employee");
    private Group jboss = new SimpleGroup("1", "jboss", null);

    @Before
    public void setup() throws Exception {
        DefaultEntitlementsManager dm = new DefaultEntitlementsManager();

        // We have a directory called "home"
        DirResource home = new DirResource("home");
        DirResource anil = new DirResource("anil");
        home.add(anil); // /home/anil

        FileResource fileA = new FileResource("filea");
        FileResource fileB = new FileResource("fileb");
        FileResource fileC = new FileResource("filec");
        DirResource work = new DirResource("work");

        // anil directory has 3 files and 1 directory called work
        anil.add(fileA).add(fileB).add(fileC).add(work);

        Entitlement readEntitlement = new SimpleEntitlement("read");
        Entitlement writeEntitlement = new SimpleEntitlement("write");
        Entitlement executeEntitlement = new SimpleEntitlement("execute");

        EntitlementCollection rwx = EntitlementCollection.create("rwx", new Entitlement[] { readEntitlement, writeEntitlement,
                executeEntitlement });
        EntitlementCollection rw = EntitlementCollection.create("rwx", new Entitlement[] { readEntitlement, writeEntitlement });
        EntitlementCollection r = EntitlementCollection.create("rwx", new Entitlement[] { readEntitlement });

        EntitlementStore store = dm.store();
        store.addUserEntitlements(fileA, useranil, rwx);
        store.addUserEntitlements(fileB, useranil, rwx);
        store.addUserEntitlements(fileC, useranil, rwx);
        store.addUserEntitlements(work, useranil, rwx);

        store.addRoleEntitlements(fileA, employee, rw);
        store.addRoleEntitlements(fileA, employee, rw);
        store.addRoleEntitlements(fileA, employee, rw);
        store.addRoleEntitlements(fileA, employee, rw);

        store.addGroupEntitlements(fileA, jboss, r);
        store.addGroupEntitlements(fileA, jboss, r);
        store.addGroupEntitlements(fileA, jboss, r);
        store.addGroupEntitlements(fileA, jboss, r);

        mgr = dm;
    }

    @Test
    public void testFilePermissions() throws Exception {
        Resource fileA = new FileResource("filea");
        UserContext userContext = new UserContext();
        List<Role> roles = new ArrayList<Role>();
        roles.add(employee);

        List<Group> groups = new ArrayList<Group>();
        groups.add(jboss);

        userContext.setUser(useranil).setRoles(roles).setGroups(groups);

        EntitlementCollection enCollection = mgr.entitlements(fileA, userContext);
        assertTrue(enCollection.contains(new SimpleEntitlement("read")));
        assertTrue(enCollection.contains(new SimpleEntitlement("write")));
        assertTrue(enCollection.contains(new SimpleEntitlement("execute")));

        // Change the user context a bit to check roles have write permission
        userContext = new UserContext();
        userContext.setUser(new SimpleUser("Bond")).setRoles(roles).setGroups(groups);
        enCollection = mgr.entitlements(fileA, userContext);
        assertTrue(enCollection.contains(new SimpleEntitlement("read")));
        assertTrue(enCollection.contains(new SimpleEntitlement("write")));
        assertFalse(enCollection.contains(new SimpleEntitlement("execute")));

        // Change the user context a bit to check groups have read permission
        userContext = new UserContext();
        userContext.setUser(null).setRoles(null).setGroups(groups);
        enCollection = mgr.entitlements(fileA, userContext);
        assertTrue(enCollection.contains(new SimpleEntitlement("read")));
        assertFalse(enCollection.contains(new SimpleEntitlement("write")));
        assertFalse(enCollection.contains(new SimpleEntitlement("execute")));
    }

    @Test
    public void testBadUserFilePermissions() throws Exception {
        Resource fileA = new FileResource("filea");
        UserContext userContext = new UserContext();
        List<Role> roles = new ArrayList<Role>();
        roles.add(new SimpleRole("banned"));

        List<Group> groups = new ArrayList<Group>();
        groups.add(new SimpleGroup("2", "bannedgroup", null));

        userContext.setUser(new SimpleUser("baduser")).setRoles(roles).setGroups(groups);

        EntitlementCollection enCollection = mgr.entitlements(fileA, userContext);
        assertFalse(enCollection.contains(new SimpleEntitlement("read")));
    }

    public static class FileResource extends SimpleResource implements Resource {
        private static final long serialVersionUID = 1L;

        public FileResource(String name) {
            super(name);
        }
    }

    public static class DirResource extends FileResource implements Resource {
        private static final long serialVersionUID = 1L;
        private List<FileResource> files = new ArrayList<FileResource>();

        public DirResource(String name) {
            super(name);
        }

        public DirResource add(FileResource file) {
            this.files.add(file);
            return this;
        }

        public DirResource add(List<FileResource> files) {
            this.files.addAll(files);
            return this;
        }
    }
}