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

package com.dbn.oracleAI.editor.action;

import com.dbn.oracleAI.types.ActionAIType;

/**
 * Editor menu action to invoke AI-Assistant with "Explain SQL" instruction
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 */
public class AssistantExplainSqlAction extends AssistantBaseEditorAction {

  @Override
  protected ActionAIType getAction() {
    return ActionAIType.EXPLAIN_SQL;
  }
}
