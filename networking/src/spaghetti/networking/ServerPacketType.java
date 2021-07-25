package spaghetti.networking;

import java.io.Serializable;

public enum ServerPacketType implements Serializable {
    SIDE,
    START_GAME,
    QUIT,
    TEST_CONNECTION,
    NAMES,
}
