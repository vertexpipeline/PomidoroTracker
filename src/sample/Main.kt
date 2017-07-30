package sample

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class Main : Application() {

    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(Main::class.java.getResource("sample.fxml"))
        primaryStage.title = "Time mamager"
        primaryStage.scene = Scene(root, 300.0, 300.0)
        primaryStage.show()
        primaryStage.isResizable = false
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            Application.launch(Main::class.java)
        }
    }
}
