package com.example.quickbedwars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;

@Mod(
        modid = "quickbedwars",
        name = "QuickBedWars",
        version = "3.0",
        clientSideOnly = true
)
public class QuickBedWars {

    private final Minecraft mc = Minecraft.getMinecraft();

    private KeyBinding key;

    private boolean waitingForGui = false;
    private boolean clicked = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        key = new KeyBinding(
                "Quick BedWars NPC",
                Keyboard.KEY_L,
                "QuickBedWars"
        );

        ClientRegistry.registerKeyBinding(key);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {

        if (!key.isPressed())
            return;

        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (waitingForGui)
            return;

        EntityPlayer npc = findNearestNPC();

        if (npc == null) {
            mc.thePlayer.addChatMessage(
                    new ChatComponentText(
                            "§c[QuickBedWars] No NPC nearby."
                    )
            );
            return;
        }

        // Face NPC
        faceNPC(npc);

        // Right click NPC
        mc.playerController.interactWithEntitySendPacket(
                mc.thePlayer,
                npc
        );

        waitingForGui = true;
        clicked = false;
    }

    /*
     * Find the nearest fake-player NPC.
     * Real players are normally present in the tab list.
     */
    private EntityPlayer findNearestNPC() {

        EntityPlayer closest = null;
        double closestDistance = 36.0D; // 6 blocks

        List<EntityPlayer> players =
                mc.theWorld.playerEntities;

        for (EntityPlayer player : players) {

            if (player == mc.thePlayer)
                continue;

            double distance =
                    mc.thePlayer.getDistanceSqToEntity(player);

            if (distance > closestDistance)
                continue;

            if (isRealPlayer(player))
                continue;

            closestDistance = distance;
            closest = player;
        }

        return closest;
    }

    /*
     * NPCs are usually not registered in the server tab list.
     */
    private boolean isRealPlayer(EntityPlayer player) {

        if (mc.getNetHandler() == null)
            return false;

        for (NetworkPlayerInfo info :
                mc.getNetHandler().getPlayerInfoMap()) {

            if (info.getGameProfile().getId()
                    .equals(player.getUniqueID())) {

                return true;
            }
        }

        return false;
    }

    private void faceNPC(EntityPlayer npc) {

        double x =
                npc.posX - mc.thePlayer.posX;

        double y =
                npc.posY +
                npc.getEyeHeight() -
                (mc.thePlayer.posY +
                mc.thePlayer.getEyeHeight());

        double z =
                npc.posZ - mc.thePlayer.posZ;

        double horizontal =
                Math.sqrt(x * x + z * z);

        float yaw =
                (float)
                (Math.atan2(z, x)
                * 180.0D / Math.PI)
                - 90.0F;

        float pitch =
                (float)
                -(Math.atan2(y, horizontal)
                * 180.0D / Math.PI);

        mc.thePlayer.rotationYaw = yaw;
        mc.thePlayer.rotationPitch = pitch;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END)
            return;

        if (!waitingForGui)
            return;

        if (mc.thePlayer == null)
            return;

        if (!(mc.thePlayer.openContainer instanceof ContainerChest))
            return;

        if (clicked)
            return;

        ContainerChest chest =
                (ContainerChest) mc.thePlayer.openContainer;

        /*
         * First green BedWars item:
         *
         * Slot 11
         */
        if (chest.inventorySlots.size() > 11) {

            mc.playerController.windowClick(
                    chest.windowId,
                    11,
                    0,
                    0,
                    mc.thePlayer
            );

            clicked = true;
            waitingForGui = false;
        }
    }
}
