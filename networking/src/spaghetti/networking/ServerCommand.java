package spaghetti.networking;

import java.io.Serializable;

public enum ServerCommand implements Serializable {
    START,
    QUIT,
    TEST_CONNECTION
}
