package local.pbaranowski.chat.formatters;

import local.pbaranowski.chat.Message;

public interface LogFormatter {
    String fromMessageToString(Message message);
}
