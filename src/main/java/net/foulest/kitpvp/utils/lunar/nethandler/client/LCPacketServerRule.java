package net.foulest.kitpvp.utils.lunar.nethandler.client;

import net.foulest.kitpvp.utils.lunar.nethandler.ByteBufWrapper;
import net.foulest.kitpvp.utils.lunar.nethandler.LCPacket;
import net.foulest.kitpvp.utils.lunar.nethandler.obj.ServerRule;
import net.foulest.kitpvp.utils.lunar.nethandler.shared.LCNetHandler;

import java.io.IOException;

public class LCPacketServerRule extends LCPacket {

    private ServerRule rule;
    private int intValue;
    private float floatValue;
    private boolean booleanValue;
    private String stringValue;

    public LCPacketServerRule() {
        this.stringValue = "";
    }

    public LCPacketServerRule(ServerRule rule, float value) {
        this.stringValue = "";
        this.rule = rule;
        this.floatValue = value;
    }

    public LCPacketServerRule(ServerRule rule, boolean value) {
        this.stringValue = "";
        this.rule = rule;
        this.booleanValue = value;
    }

    public LCPacketServerRule(ServerRule rule, int value) {
        this.stringValue = "";
        this.rule = rule;
        this.intValue = value;
    }

    public LCPacketServerRule(ServerRule rule, String value) {
        this.stringValue = "";
        this.rule = rule;
        this.stringValue = value;
    }

    @Override
    public void write(ByteBufWrapper buf) throws IOException {
        buf.writeString(rule.getId());
        buf.buf().writeBoolean(booleanValue);
        buf.buf().writeInt(intValue);
        buf.buf().writeFloat(floatValue);
        buf.writeString(stringValue);
    }

    @Override
    public void read(ByteBufWrapper buf) throws IOException {
        rule = ServerRule.getRule(buf.readString());
        booleanValue = buf.buf().readBoolean();
        intValue = buf.buf().readInt();
        floatValue = buf.buf().readFloat();
        stringValue = buf.readString();
    }

    @Override
    public void process(LCNetHandler handler) {
        ((LCNetHandlerClient) handler).handleServerRule(this);
    }

    public ServerRule getRule() {
        return rule;
    }

    public int getIntValue() {
        return intValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
