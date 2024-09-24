/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.assistant.profile.action;

import com.dbn.assistant.profile.ui.ProfileManagementForm;
import com.dbn.common.action.DataKeys;
import com.dbn.common.action.ProjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic stub for actions related to management of profiles
 * (features the lookup of the profile management form from the context)
 *
 * @author Dan Cioca (Oracle)
 */
public abstract class ProfileManagementAction extends ProjectAction {
    protected static @Nullable ProfileManagementForm getManagementForm(@NotNull AnActionEvent e) {
        return e.getData(DataKeys.PROFILE_MANAGEMENT_FORM);
    }
}
