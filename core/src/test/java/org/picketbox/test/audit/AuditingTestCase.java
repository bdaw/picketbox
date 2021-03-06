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

package org.picketbox.test.audit;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.UserContext;
import org.picketbox.core.audit.AuditType;
import org.picketbox.core.audit.providers.LogAuditProvider;
import org.picketbox.core.authentication.credential.UsernamePasswordCredential;
import org.picketbox.core.config.ConfigurationBuilder;
import org.picketbox.test.AbstractDefaultPicketBoxManagerTestCase;

/**
 * <p>
 * Tests the auditing capabilities.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AuditingTestCase extends AbstractDefaultPicketBoxManagerTestCase {

    /**
     * <p>
     * Tests the default configuration for auditing. By default, PicketBox will use the {@link LogAuditProvider} to log audit
     * events.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testDefaultAuditingConfiguration() throws Exception {
        ConfigurationBuilder builder = new ConfigurationBuilder();

        builder.audit().logProvider();

        PicketBoxManager picketBoxManager = createManager(builder);

        UserContext authenticatingUserContext = new UserContext();

        authenticatingUserContext.setCredential(new UsernamePasswordCredential("admin", "admin"));

        UserContext subject = picketBoxManager.authenticate(authenticatingUserContext);

        assertNotNull(subject);
        assertTrue(subject.isAuthenticated());
    }

    /**
     * <p>
     * Tests the default configuration for auditing. By default, PicketBox will use the {@link LogAuditProvider} to log audit
     * events.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testCustomAuditingProvider() throws Exception {
        ConfigurationBuilder builder = new ConfigurationBuilder();

        CustomAuditProvider customAuditProvider = new CustomAuditProvider();

        builder.audit().provider(customAuditProvider);

        PicketBoxManager picketBoxManager = createManager(builder);

        UserContext authenticatingUserContext = new UserContext();

        authenticatingUserContext.setCredential(new UsernamePasswordCredential("admin", "admin"));

        UserContext subject = picketBoxManager.authenticate(authenticatingUserContext);

        assertNotNull(subject);
        assertTrue(subject.isAuthenticated());

        assertTrue(customAuditProvider.isAudited());
    }

    /**
     * <p>
     * Tests the default configuration for auditing. By default, PicketBox will use the {@link LogAuditProvider} to log audit
     * events.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testAuditEventsHandling() throws Exception {
        ConfigurationBuilder builder = new ConfigurationBuilder();

        MockUserAuditEventHandler auditEventHandler = new MockUserAuditEventHandler();

        builder.audit().logProvider().eventManager().handler(auditEventHandler);

        PicketBoxManager picketBoxManager = createManager(builder);

        UserContext authenticatingUserContext = new UserContext();

        authenticatingUserContext.setCredential(new UsernamePasswordCredential("admin", "admin"));

        UserContext subject = picketBoxManager.authenticate(authenticatingUserContext);

        assertNotNull(subject);
        assertTrue(subject.isAuthenticated());

        assertTrue(auditEventHandler.isPreAuditEvent());
        assertTrue(auditEventHandler.isPostAuditEvent());
        assertNotNull(auditEventHandler.getEvent());
        assertEquals(AuditType.AUTHENTICATION, auditEventHandler.getEvent().getAuditType());
        assertNotNull(auditEventHandler.getEvent().getCreationDate());
        assertNotNull(auditEventHandler.getEvent().getDescription());
        assertNotNull(auditEventHandler.getEvent().getUserContext());
        assertTrue(auditEventHandler.getEvent().getContextMap().containsKey("customAuditInfo"));
    }

}