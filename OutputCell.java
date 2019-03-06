package CopyCatch;

/*
 * Copy Catch
 * Copyright 2018 Dr. Colvin
 * with students: Joshua Styger & Brandon Tran
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

class OutputCell extends HBox {
    private Label label = new Label();
    private Button viewFiles = new Button();
    double score;

    OutputCell(File firstFile, File secondFile, double score) {
        super();
        this.score = score;
        label.setText(GetXCharString(15,firstFile.getName()) + " is " + score
                + "% similar to assignment " + GetXCharString(15,secondFile.getName()));
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);

        viewFiles.setText("View files");

        viewFiles.setOnAction(e -> {
            Stage dialog = new Stage();
            DisplayTextFiles.viewFiles(dialog, firstFile, secondFile);
        });

        this.getChildren().addAll(label, viewFiles);
    }

    
    private String GetXCharString(int size, String str)
    {
    	if (str.length() > size)
    	{
    		return str.substring(0, size) + '~';
    	}
    	else
    	{
    		String s = "%-" + size + "s";
    		return String.format(s, str);
    	}
    				
    }
}
