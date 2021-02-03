package net.foulest.kitpvp.utils.lunar.nethandler;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ByteBufWrapper {

    private final ByteBuf buf;

    public ByteBufWrapper(ByteBuf buf) {
        this.buf = buf;
    }

    public void writeVarInt(int b) {
        while ((b & 0xFFFFFF80) != 0x0) {
            buf.writeByte((b & 0x7F) | 0x80);
            b >>>= 7;
        }

        buf.writeByte(b);
    }

    public int readVarInt() {
        int i = 0;
        int chunk = 0;
        byte b;

        do {
            b = buf.readByte();
            i |= (b & 0x7F) << chunk++ * 7;

            if (chunk > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b & 0x80) == 0x80);

        return i;
    }

    public void writeString(String s) {
        byte[] arr = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(arr.length);
        buf.writeBytes(arr);
    }

    public String readString() {
        int len = readVarInt();
        byte[] buffer = new byte[len];
        buf.readBytes(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public void writeUUID(UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public UUID readUUID() {
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    public ByteBuf buf() {
        return buf;
    }
}
