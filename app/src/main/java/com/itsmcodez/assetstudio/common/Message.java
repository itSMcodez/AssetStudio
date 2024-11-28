package com.itsmcodez.assetstudio.common;
import com.itsmcodez.assetstudio.markers.MessageType;

public interface Message {
    MessageType getType();
    String getDescription();
}
