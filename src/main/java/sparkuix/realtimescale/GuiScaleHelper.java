package sparkuix.realtimescale;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ForgeHooksClient;

import static net.minecraft.client.Minecraft.ON_OSX;


public class GuiScaleHelper {
    private final Minecraft mc = Minecraft.getInstance();
    private final Window window=mc.getWindow();
    private final Screen screen=mc.screen;
    public static void setGuiScale(double scale) {
        GuiScaleHelper guiScaleHelper = new GuiScaleHelper();
        guiScaleHelper.
                stg(scale);

    }

    public void stg(double ss) {

        this.window.setGuiScale((double)ss);
        if (this.screen != null) {
            this.screen.resize(mc, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
            ForgeHooksClient.resizeGuiLayers(mc, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
        }

        RenderTarget rendertarget = mc.getMainRenderTarget();
        rendertarget.resize(this.window.getWidth(), this.window.getHeight(), ON_OSX);
        mc.gameRenderer.resize(this.window.getWidth(), this.window.getHeight());

        mc.mouseHandler.setIgnoreFirstMove();
    }

}
