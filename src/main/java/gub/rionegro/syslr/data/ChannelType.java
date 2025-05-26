package gub.rionegro.syslr.data;


public enum ChannelType {
    WHATSAPP("WhatsApp"),
    TELEGRAM("Telegram"),
    WEB("Web Chat"),
    SMS("SMS"),
    FACEBOOK("Facebook Messenger"),
    INSTAGRAM("Instagram"),
    OTHER("Otro");

    private final String displayName;

    ChannelType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ChannelType fromString(String text) {
        for (ChannelType channel : ChannelType.values()) {
            if (channel.name().equalsIgnoreCase(text)) {
                return channel;
            }
        }
        return OTHER;
    }
}