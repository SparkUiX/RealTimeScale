package sparkuix.realtimescale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber
public class ScrollHandler {
    private static double scrollValue=2;
    @SubscribeEvent
    public void changeGui(ScreenOpenEvent event) {
        RealTimeScaleConfig realTimeScaleConfig = new RealTimeScaleConfig();

        Screen eventScreen = event.getScreen();
//        if (event.getScreen() != null)
//            System.out.println("打开的GUI: " + event.getScreen().getClass().toString());
        if (event.getScreen() == null) {
            double defaultScale=realTimeScaleConfig.readDoubleDefaultScale();
//            realTimeScaleConfig.writeCustomScale(event.getScreen().toString(),scrollValue);
            GuiScaleHelper.setGuiScale(defaultScale);
            System.out.println("所有GUI都已关闭");
        } else
        if(!(eventScreen instanceof TitleScreen || eventScreen instanceof SelectWorldScreen || eventScreen instanceof GenericDirtMessageScreen || eventScreen instanceof ProgressScreen || eventScreen instanceof ReceivingLevelScreen || eventScreen instanceof LevelLoadingScreen||eventScreen instanceof CreativeModeInventoryScreen||eventScreen instanceof CreateWorldScreen))
        {
            double customScale=realTimeScaleConfig.readCustomScale(event.getScreen().getClass().toString());
            scrollValue=customScale;
            GuiScaleHelper.setGuiScale(customScale);
//            System.out.println("打开的GUI: " + event.getScreen().getClass().toString()+"\n数值:"+customScale);
        } else if (eventScreen instanceof CreativeModeInventoryScreen) {
            double customScale=realTimeScaleConfig.readCustomScale("class net.minecraft.client.gui.screens.inventory.InventoryScreen");
            scrollValue=customScale;
//            System.out.println("打开的创造GUI: " + event.getScreen().getClass().toString()+"\n数值:"+customScale);
        }
    }
    @SubscribeEvent
    public static void onScreenMouseScroll(ScreenEvent.MouseScrollEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        RealTimeScaleConfig realTimeScaleConfig = new RealTimeScaleConfig();
        if (mc.player == null) return;

        int maxScale = Minecraft.getInstance()
                .getWindow()
                .calculateScale(0, Minecraft.getInstance().isEnforceUnicode());

        long window = mc.getWindow().getWindow();

        // CTRL 和 Shift 按键是否按下
        boolean isCtrlDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        boolean isShiftDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

        // 如果按下 CTRL
        if (isCtrlDown) {
            double scrollDelta = event.getScrollDelta();

            // 判断是否按下 SHIFT，如果按下 Shift，则使用更小的增量
            double step = isShiftDown ? 0.01 : 0.1;

            // 向上滚动（减少值）
            if (scrollDelta < 0 && scrollValue >= 0) {
                scrollValue -= step;
            }
            // 向下滚动（增加值）
            else if (scrollDelta > 0) {
                scrollValue += step;
            }

            // 防止 scrollValue 小于 0
            if (scrollValue < 0)
                scrollValue = 0.01;

            // 打印新变量值
//            System.out.println("当前 scrollValue 值: " + scrollValue);

            String UIname = event.getScreen().getClass().toString();
            if(UIname.equals("class net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen"))
            {
                UIname="class net.minecraft.client.gui.screens.inventory.InventoryScreen";
            }
            realTimeScaleConfig.writeCustomScale(UIname,scrollValue);
            GuiScaleHelper guiScaleHelper = new GuiScaleHelper();


            GuiScaleHelper.setGuiScale(scrollValue);
        }
    }
    @SubscribeEvent
    public static void onKeyPress(ScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        // 判断是否是 'L' 键
        if (event.getKeyCode() == GLFW.GLFW_KEY_N) {
            // 检测修饰符中是否包含 CTRL
            // GLFW.GLFW_MOD_CONTROL 通常为 2（具体值因版本而异）
            if ((event.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
                // 在此执行你想要的方法操作
                System.out.println("检测到按下了 CTRL + N！");
                new RealTimeScaleConfig().writeDoubleDefaultScale(scrollValue);
            }
        }
    }
}
