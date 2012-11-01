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

package org.picketbox.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.picketbox.core.DefaultPicketBoxManager;
import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.UserContext;
import org.picketbox.core.config.ConfigurationBuilder;
import org.picketbox.core.config.PicketBoxConfiguration;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.PasswordCredential;
import org.picketlink.idm.credential.X509CertificateCredential;
import org.picketlink.idm.file.internal.FileUser;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;

/**
 * <p>
 * Base class for test cases that allows to create a fresh {@link PicketBoxManager} instance using some specific
 * {@link PicketBoxConfiguration}. This class also initializes the identity store with the default user information.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractDefaultPicketBoxManagerTestCase {

    private PicketBoxManager picketboxManager;

    protected PicketBoxManager getPicketBoxManager(PicketBoxConfiguration configuration) {
        if (this.picketboxManager == null) {
            this.picketboxManager = new DefaultPicketBoxManager(configuration);
            this.picketboxManager.start();
            initialize(this.picketboxManager.getIdentityManager());
        }

        return this.picketboxManager;
    }
    
    /**
     * <p>
     * Creates a {@link PicketBoxManager}.
     * </p>
     *
     * @return
     */
    protected PicketBoxManager createManager(ConfigurationBuilder... builder) {
        ConfigurationBuilder configBuilder = null;

        if (builder.length == 0) {
            configBuilder = new ConfigurationBuilder();
        } else {
            configBuilder = builder[0];
        }
        
        return getPicketBoxManager(configBuilder.build());
    }

    /**
     * <p>
     * Initializes the identity manager store with users information.
     * </p>
     *
     * @param identityManager
     */
    private void initialize(IdentityManager identityManager) {
        FileUser adminUser = new FileUser("admin");

        identityManager.createUser(adminUser);

        adminUser.setEmail("admin@picketbox.com");
        adminUser.setFirstName("The");
        adminUser.setLastName("Admin");

        identityManager.updateCredential(adminUser, new PasswordCredential("admin"));
        identityManager.updateCredential(adminUser, new X509CertificateCredential(getTestingCertificate()));

        Role roleDeveloper = identityManager.createRole("developer");
        Role roleAdmin = identityManager.createRole("admin");

        Group groupCoreDeveloper = identityManager.createGroup("PicketBox Group");

        identityManager.grantRole(roleDeveloper, adminUser, groupCoreDeveloper);
        identityManager.grantRole(roleAdmin, adminUser, groupCoreDeveloper);
        
        FileUser jbidTestUser = new FileUser("jbid test");

        identityManager.createUser(jbidTestUser);

        identityManager.updateCredential(jbidTestUser, new X509CertificateCredential(getTestingCertificate()));

        identityManager.grantRole(roleDeveloper, jbidTestUser, groupCoreDeveloper);
        identityManager.grantRole(roleAdmin, jbidTestUser, groupCoreDeveloper);

        FileUser certUser = new FileUser("CN=jbid test, OU=JBoss, O=JBoss, C=US");

        identityManager.createUser(certUser);
        
        identityManager.updateCredential(certUser, new X509CertificateCredential(getTestingCertificate()));
        
        identityManager.grantRole(roleDeveloper, certUser, groupCoreDeveloper);
        identityManager.grantRole(roleAdmin, certUser, groupCoreDeveloper);
    }
    
    protected void assertRoles(UserContext authenticatedUser) {
        assertFalse(authenticatedUser.getRoles().isEmpty());
        assertTrue(authenticatedUser.getRoles().containsAll(Arrays.asList(new Role[] {new SimpleRole("developer"), new SimpleRole("admin")})));
    }

    protected void assertGroups(UserContext authenticatedUser) {
        assertFalse(authenticatedUser.getGroups().isEmpty());
        assertTrue(authenticatedUser.getGroups().containsAll(Arrays.asList(new Group[] {new SimpleGroup("PicketBox Group","PicketBox Group",null)})));
    }

    protected X509Certificate getTestingCertificate() {
        // Certificate
        InputStream bis = getClass().getClassLoader().getResourceAsStream("cert/servercert.txt");
        X509Certificate cert = null;

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(bis);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load testing certificate.", e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }
        }
        return cert;
    }

}
