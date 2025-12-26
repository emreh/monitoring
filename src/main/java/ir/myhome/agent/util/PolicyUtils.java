package ir.myhome.agent.util;

public final class PolicyUtils {

    public static long fastHash64(String str) {
        final long FNV_64_PRIME = 0x100000001b3L;
        long hash = 0xcbf29ce484222325L; // FNV offset basis

        for (int i = 0; i < str.length(); i++) {
            hash ^= str.charAt(i);
            hash *= FNV_64_PRIME;
        }

        return hash;
    }
}
