package ulb.view;

import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.Test;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ulb.controller.MainController;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

public class GuiTest extends ApplicationTest {

    private Stage stage;
    private MainController controller;

    @Override
    public void init() {
        File saveFile = new File("teams_save.json");
        if (saveFile.exists()) {
            saveFile.delete();
        }
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        controller = new MainController(stage);
        controller.showMainMenu();
    }

    @Test
    public void shouldShowMainMenuOnStartup() {
        Assertions.assertThat(stage.isShowing()).isTrue();
        verifyThat("#main-title", hasText(containsString("Bug")));
        verifyThat("#manageTeam", isVisible());
    }

    @Test
    public void shouldOpenTeamManagementScreen() {
        interact(() -> controller.showTeamManagement());
        waitForNode("#backButton");
        verifyThat("#backButton", isVisible());
        verifyThat("#nameLabel", isVisible());
    }

    @Test
    public void shouldShowPopupWhenNotificationIsTriggered() {
        long visibleWindowsBeforeSave = countVisibleWindows();
        interact(() -> controller.showNotification("Popup de test"));

        waitForAdditionalVisibleWindow(visibleWindowsBeforeSave);
        Assertions.assertThat(countVisibleWindows()).isGreaterThan(visibleWindowsBeforeSave);
        verifyThat("#gameNotificationMessage", hasText("Popup de test"));
    }

    private void waitForNode(String selector) {
        for (int attempt = 0; attempt < 30; attempt++) {
            WaitForAsyncUtils.waitForFxEvents();
            if (!lookup(selector).queryAll().isEmpty()) {
                return;
            }
            sleep(100);
        }
        throw new AssertionError("Node not found: " + selector);
    }

    private long countVisibleWindows() {
        return Window.getWindows().stream().filter(Window::isShowing).count();
    }

    private void waitForAdditionalVisibleWindow(long previousCount) {
        for (int attempt = 0; attempt < 30; attempt++) {
            WaitForAsyncUtils.waitForFxEvents();
            if (countVisibleWindows() > previousCount) {
                return;
            }
            sleep(100);
        }
        throw new AssertionError("Popup window not shown.");
    }
}
