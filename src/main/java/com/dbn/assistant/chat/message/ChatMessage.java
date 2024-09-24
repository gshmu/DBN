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

package com.dbn.assistant.chat.message;

import com.dbn.assistant.chat.window.PromptAction;
import com.dbn.assistant.editor.SQLChatMessageConverter;
import com.dbn.common.latent.Latent;
import com.dbn.common.message.MessageType;
import com.dbn.common.util.UUIDs;
import com.dbn.language.sql.SQLLanguage;
import com.intellij.lang.Language;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {
    private static final Pattern SECTIONS_REGEX_PATTERN = Pattern.compile("(?s)(?:(.*?)```(\\w+)?\\n(.*?)```|(.+?)(?=\\n?```|\\z))");

    /**
     * Unique identifier of the chat message to establish causality relations and chaining of messages
     */
    protected String id = UUIDs.regular();

    protected MessageType type = MessageType.NEUTRAL;
    protected AuthorType author;
    protected String content;
    protected ChatMessageContext context;
    private Latent<List<ChatMessageSection>> sections = Latent.basic(() -> buildSections());

    private transient boolean progress;

    /**
     * Creates a new ChatMessage
     *
     * @param type the message type (relevant for SYSTEM messages)
     * @param content the message content
     * @param author  the author of the message
     * @param context the context in which the chat message was produced
     */
    public ChatMessage(MessageType type, String content, AuthorType author, ChatMessageContext context) {
        this.type = type;
        this.content = content;
        this.author = author;
        this.context = context;
    }

    public List<ChatMessageSection> getSections() {
        return sections.get();
    }

    /**
     * Breaks message contents into sections, to allow different styling of the content within same response.
     * Background: responses from the AI backends may contain a sequence of text and code sections.
     * Code is typically demarcated by ``` (3 single quotes) followed by code content and closed with again with 3 single quotes
     *
     * @return a list of {@link ChatMessageSection} with the different sections
     */
    private List<ChatMessageSection> buildSections() {
        if (author == AuthorType.AGENT && context.getAction() == PromptAction.SHOW_SQL && !content.contains("```")) {
            // output is already expected to be SQL code based on the action
            // TODO not always true (workaround -> enhance the prompt with "(please use code demarcation with language identifier in the output)")
            return new ChatMessageSection(content.trim(), "sql").asList();
        }

        if (author.isOneOf(AuthorType.USER, AuthorType.SYSTEM)) {
            // output is already expected to be plain text
            return new ChatMessageSection(content.trim(), null).asList();
        }

        Matcher matcher = SECTIONS_REGEX_PATTERN.matcher(content);

        List<ChatMessageSection> sections = new ArrayList<>();
        while (matcher.find()) {
            String leadingText = matcher.group(1);
            String languageId = matcher.group(2);
            String codeContent = matcher.group(3);
            String tailingText = matcher.group(4);

            createMessageSection(leadingText, null, sections);
            createMessageSection(codeContent, languageId, sections);
            createMessageSection(tailingText, null, sections);
        }

        return sections;
    }


    private static void createMessageSection(@Nullable String content, @Nullable String languageId, List<ChatMessageSection> container) {
        if (content == null || content.isBlank()) return;
        container.add(new ChatMessageSection(content.trim(), languageId));
    }

    public String outputForLanguage(Language language) {
        if (language == SQLLanguage.INSTANCE) {
            return SQLChatMessageConverter.INSTANCE.convert(this);
            //.. TODO more languages if functionality is integrated in non-SQL editors
        }
        return content;
    }
}
