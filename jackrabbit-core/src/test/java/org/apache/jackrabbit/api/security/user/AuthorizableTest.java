/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.api.security.user;

import org.apache.jackrabbit.test.NotExecutableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.security.Principal;
import java.util.Arrays;
import java.util.Iterator;

/**
 * <code>UserTest</code>...
 */
public class AuthorizableTest extends AbstractUserTest {

    private static Logger log = LoggerFactory.getLogger(AuthorizableTest.class);

    public void testGetId() throws NotExecutableException, RepositoryException {
        User user = getTestUser(superuser);
        assertNotNull(user.getID());
    }

    public void testGroupGetId() throws NotExecutableException, RepositoryException {
        Group gr = getTestGroup(superuser);
        assertNotNull(gr.getID());
    }

    public void testGetPrincipalNotNull() throws RepositoryException, NotExecutableException {
        User user = getTestUser(superuser);
        assertNotNull(user.getPrincipal());
    }

    public void testGroupGetPrincipalNotNull() throws RepositoryException, NotExecutableException {
        Group gr = getTestGroup(superuser);
        assertNotNull(gr.getPrincipal());
    }

    public void testSetProperty() throws NotExecutableException, RepositoryException {
        Authorizable auth = getTestUser(superuser);

        // TODO: retrieve propname and value from config
        String propName = "Fullname";
        Value v = superuser.getValueFactory().createValue("Super User");
        try {
            auth.setProperty(propName, v);
        } catch (RepositoryException e) {
            throw new NotExecutableException("Cannot test 'Authorizable.setProperty'.");
        }

        try {
            boolean found = false;
            for (Iterator it = auth.getPropertyNames(); it.hasNext() && !found;) {
                found = propName.equals(it.next().toString());
            }
            assertTrue(found);
            assertTrue(auth.hasProperty(propName));
            assertTrue(auth.getProperty(propName).length == 1);
            assertEquals(v, auth.getProperty(propName)[0]);
            assertTrue(auth.removeProperty(propName));
        } finally {
            // try to remove the property again even if previous calls failed.
            auth.removeProperty(propName);
        }
    }

    public void testSetMultiValueProperty() throws NotExecutableException, RepositoryException {
        Authorizable auth = getTestUser(superuser);

        // TODO: retrieve propname and values from config
        String propName = "Fullname";
        Value[] v = new Value[] {superuser.getValueFactory().createValue("Super User")};
        try {
            auth.setProperty(propName, v);
        } catch (RepositoryException e) {
            throw new NotExecutableException("Cannot test 'Authorizable.setProperty'.");
        }

        try {
            boolean found = false;
            for (Iterator it = auth.getPropertyNames(); it.hasNext() && !found;) {
                found = propName.equals(it.next().toString());
            }
            assertTrue(found);
            assertTrue(auth.hasProperty(propName));
            assertEquals(Arrays.asList(v), Arrays.asList(auth.getProperty(propName)));
            assertTrue(auth.removeProperty(propName));
        } finally {
            // try to remove the property again even if previous calls failed.
            auth.removeProperty(propName);
        }
    }

    public void testGetPropertyNames() throws NotExecutableException, RepositoryException {
        Authorizable auth = getTestUser(superuser);

        // TODO: retrieve propname and value from config
        String propName = "Fullname";
        Value v = superuser.getValueFactory().createValue("Super User");
        try {
            auth.setProperty(propName, v);
        } catch (RepositoryException e) {
            throw new NotExecutableException("Cannot test 'Authorizable.setProperty'.");
        }

        try {
            for (Iterator it = auth.getPropertyNames(); it.hasNext();) {
                String name = it.next().toString();
                assertTrue(auth.hasProperty(name));
                assertNotNull(auth.getProperty(name));
            }
        } finally {
            // try to remove the property again even if previous calls failed.
            auth.removeProperty(propName);
        }
    }

    public void testGetNotExistingProperty() throws RepositoryException, NotExecutableException {
        Authorizable auth = getTestUser(superuser);
        String hint = "Fullname";
        String propName = hint;
        int i = 0;
        while (auth.hasProperty(propName)) {
            propName = hint + i;
            i++;
        }
        assertNull(auth.getProperty(propName));
        assertFalse(auth.hasProperty(propName));
    }

    public void testRemoveNotExistingProperty() throws RepositoryException, NotExecutableException {
        Authorizable auth = getTestUser(superuser);
        String hint = "Fullname";
        String propName = hint;
        int i = 0;
        while (auth.hasProperty(propName)) {
            propName = hint + i;
            i++;
        }
        assertFalse(auth.removeProperty(propName));
    }

    public void testMemberOf() throws NotExecutableException, RepositoryException {
        Authorizable auth = getTestUser(superuser);

        Iterator it = auth.memberOf();
        while (it.hasNext()) {
            Object group = it.next();
            assertTrue(group instanceof Group);
        }
    }

    public void testDeclaredMemberOf() throws NotExecutableException, RepositoryException {
        Authorizable auth = getTestUser(superuser);

        Iterator it = auth.declaredMemberOf();
        while (it.hasNext()) {
            Object group = it.next();
            assertTrue(group instanceof Group);
        }
    }

    /**
     * Removing an authorizable that is still listed as member of a group.
     */
    public void testRemoveListedAuthorizable() throws RepositoryException, NotExecutableException {
        String newUserId = null;
        Group newGroup = null;

        try {
            Principal uP = getTestPrincipal();
            User newUser = userMgr.createUser(uP.getName(), uP.getName());
            newUserId = newUser.getID();

            newGroup = userMgr.createGroup(getTestPrincipal());
            newGroup.addMember(newUser);

            // remove the new user that is still listed as member.
            newUser.remove();
        } finally {
            if (newUserId != null) {
                Authorizable u = userMgr.getAuthorizable(newUserId);
                if (u != null) {
                    if (newGroup != null) {
                        newGroup.removeMember(u);
                    }
                    u.remove();
                }
            }
            if (newGroup != null) {
                newGroup.remove();
            }
        }
    }
}
