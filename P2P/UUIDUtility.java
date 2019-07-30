import java.util.UUID;

public class UUIDUtility {
    public static int convertCharAt(String uuid, int index) {
        return convertChar(uuid.charAt(index));
    }

    public static int convertChar(char uuidChar) {
        return Integer.parseInt(Character.toString(uuidChar), 16);
    }

    public static String generateID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }
}