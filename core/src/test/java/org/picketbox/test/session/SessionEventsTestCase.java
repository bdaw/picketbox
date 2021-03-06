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
package org.picketbox.test.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.picketbox.core.DefaultPicketBoxManager;
import org.picketbox.core.PicketBoxManager;
import org.picketbox.core.UserContext;
import org.picketbox.core.config.ConfigurationBuilder;
import org.picketbox.core.session.PicketBoxSession;
import org.picketbox.core.session.SessionManager;

/**
 * Unit test the handling of session related events.
 *
 * @author anil saldhana
 * @since Jul 16, 2012
 */
public class SessionEventsTestCase {

    private SessionManager sessionManager;
    private MockSessionEventHandler testEventHandler;

    @Before
    public void onSetup() {
        ConfigurationBuilder builder = new ConfigurationBuilder();

        this.testEventHandler = new MockSessionEventHandler();

        builder.sessionManager().fileSessionStore().eventManager().handler(this.testEventHandler);

        PicketBoxManager picketBoxManager = new DefaultPicketBoxManager(builder.build());

        picketBoxManager.start();

        this.sessionManager = picketBoxManager.getSessionManager();
    }

    @Test
    public void testOnCreate() throws Exception {
        createSession();
        assertTrue(this.testEventHandler.onCreateCalled);
    }

    @Test
    public void testOnSetAttribute() throws Exception {
        PicketBoxSession session = createSession();

        session.setAttribute("a", "b");
        assertTrue(this.testEventHandler.onSetAttributeCalled);
        assertEquals("b", session.getAttribute("a"));
    }

    @Test
    public void testOnGetAttribute() throws Exception {
        PicketBoxSession session = createSession();

        session.setAttribute("a", "b");
        assertEquals("b", session.getAttribute("a"));
        assertTrue(this.testEventHandler.onGetAttributeCalled);
    }

    @Test
    public void testOnInvalidate() throws Exception {
        PicketBoxSession session = createSession();

        session.invalidate();
        assertFalse(session.isValid());
        assertTrue(this.testEventHandler.onInvalidateCalled);
    }

    @Test
    public void testOnExpire() throws Exception {
        PicketBoxSession session = createSession();

        session.expire();
        assertFalse(session.isValid());
        assertTrue(this.testEventHandler.onExpirationCalled);
    }

    @Test
    public void testGetExpire() throws Exception {
        PicketBoxSession session = createSession();

        session.expire();
        assertFalse(session.isValid());
        assertTrue(this.testEventHandler.onExpirationCalled);
    }

    private PicketBoxSession createSession() {
        UserContext subject = new UserContext();

        PicketBoxSession session = this.sessionManager.create(subject);

        assertNotNull(session);
        assertTrue(session.isValid());

        return session;
    }

}