package local.pbaranowski.chat;

public class NameValidators {
    public static boolean isNameValid(String name) {
        return name.matches("\\w{3,16}");
    }

    public static boolean isChannelName(String name) {
        return name.matches("#\\w{3,16}");
    }

    public static boolean isChannelSpecial(String name) {
        return name.matches("@\\w{3,16}");
    }

    public static boolean isNameOrChannelValid(String name) {
        return isNameValid(name) || isChannelName(name);
    }
}
