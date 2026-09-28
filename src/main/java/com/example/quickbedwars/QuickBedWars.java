package com.example.quickbedwars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(
        modid = "quickbedwars",
        name = "QuickBedWars",
        version = "1.0",
        clientSideOnly = true
)
public class QuickBedWars {

    private final Minecraft mc = Minecraft.getMinecraft();

    private KeyBinding key;
    private boolean waiting = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        key = new KeyBinding(
                "Quick BedWars",
                Keyboard.KEY_K,
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

        waiting = true;

        for (int slot = 0; slot < 9; slot++) {

            ItemStack stack =
                    mc.thePlayer.inventory.getStackInSlot(slot);

            if (stack != null &&
                    stack.getItem() == Items.compass) {

                int oldSlot =
                        mc.thePlayer.inventory.currentItem;

                mc.thePlayer.inventory.currentItem = slot;

                mc.playerController.sendUseItem(
                        mc.thePlayer,
                        mc.theWorld,
                        stack
                );

                mc.thePlayer.inventory.currentItem = oldSlot;

                return;
            }
        }

        waiting = false;

        mc.thePlayer.addChatMessage(
                new ChatComponentText(
                        "§c[QuickBedWars] Compass not found."
                )
        );
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END)
            return;

        if (!waiting || mc.thePlayer == null)
            return;

        if (mc.thePlayer.openContainer instanceof ContainerChest) {

            ContainerChest chest =
                    (ContainerChest) mc.thePlayer.openContainer;

            if (22 < chest.inventorySlots.size()) {

                mc.playerController.windowClick(
                        chest.windowId,
                        22,
                        0,
                        0,
                        mc.thePlayer
                );

                waiting = false;
            }
        }
    }
    }
