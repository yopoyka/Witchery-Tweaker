package muwa.witcherytweaker.common;

import com.emoniph.witchery.blocks.BlockWitchesOven;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import muwa.witcherytweaker.Witweaker;
import net.minecraft.client.Minecraft;

public class MessageCookTime implements IMessage {
    private int x, y, z, dim, time;

    public MessageCookTime() {
    }

    public MessageCookTime(int x, int y, int z, int dim, int time) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        this.time = time;
    }

    public static void send(BlockWitchesOven.TileEntityWitchesOven oven) {
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class Handler implements IMessageHandler<MessageCookTime, IMessage> {

        @Override
        public IMessage onMessage(MessageCookTime message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {

            });
            return null;
        }
    }
}
