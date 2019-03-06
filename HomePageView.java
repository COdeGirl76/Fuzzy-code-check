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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class HomePageView extends Application
{

	private static String assignmentDirectory;
	private static ArrayList<File> validFiles;
	private static List<FileStats> fileStats;
	private static ListView<OutputCell> outputListView;
	private static Stage dialogStage;
	private static Label progressText;
	private static boolean comparisonRan = false;
	private final int initialValue = 70;

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		initUI(primaryStage);
	}

	private void initUI(Stage primaryStage)
	{

		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/CCIcon.png")));
		// primaryStage.getIcons()add(new Image(url))
		VBox root = new VBox();
		GridPane mainArea = new GridPane();
		mainArea.setHgap(8);
		mainArea.setVgap(8);
		mainArea.setPadding(new Insets(8));

		// create all the parts of the GUI
		Button chooseDirectory = new Button("Choose Directory");
		TextField directoryPath = new TextField();
		outputListView = new ListView<OutputCell>();
		Button runProgram = new Button("Run");
		Label spinnerLabal = new Label("Similarity Threshold");
		Spinner<Integer> similarityTreshhold = new Spinner<Integer>();

		// Do menus
		MenuBar leftBar = new MenuBar();
		leftBar.getMenus().addAll(new Menu(""));
		leftBar.getMenus().get(0).setDisable(true);
		leftBar.getStyleClass().add("menuBar");
		MenuBar rightBar = new MenuBar();
		rightBar.getMenus().addAll(new Menu("Help"));
		MenuItem about = new MenuItem("About");
		rightBar.getMenus().get(0).getItems().add(about);
		rightBar.getStyleClass().add("menuBar");
		Region spacer = new Region();
		spacer.getStyleClass().add("menuBar");
		HBox.setHgrow(spacer, Priority.SOMETIMES);
		HBox menubars = new HBox(leftBar, spacer, rightBar);

		similarityTreshhold.setEditable(true);
		similarityTreshhold.setPrefWidth(80);
		outputListView.setPrefHeight(600);

		SpinnerValueFactory<Integer> valueFactory = //
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, initialValue);

		similarityTreshhold.setValueFactory(valueFactory);

		similarityTreshhold.getEditor().textProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue.length() > 3)
			{
				similarityTreshhold.getEditor().setText(oldValue);
				return;
			}
			for (int i = 0; i < newValue.length(); i++)
			{
				if (!Character.isDigit(newValue.charAt(i)))
				{
					if (newValue.charAt(i) == '&')
					{

						System.out.println("DirButton: " + chooseDirectory.getWidth());
						System.out.println("Path: " + directoryPath.getWidth());
						System.out.println("Thresh Label: " + spinnerLabal.getWidth());
						System.out.println("Spinner: " + similarityTreshhold.getWidth());
						System.out.println("RunButton: " + runProgram.getWidth());
					}
					similarityTreshhold.getEditor().setText(oldValue);
					return;
				}
			}
		});
		similarityTreshhold.focusedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue)
			{
				similarityTreshhold.increment(0); // won't change value, but will commit editor
			}
		});

		similarityTreshhold.valueProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue == null)
			{
				similarityTreshhold.getValueFactory().setValue(oldValue);
			}
			if (comparisonRan && newValue <= 100)
			{
				DisplayResults(newValue);
			}
		});

		DirectoryChooser directoryChooser = new DirectoryChooser();

		ColumnConstraints cons1 = new ColumnConstraints();
		cons1.setHgrow(Priority.NEVER);

		ColumnConstraints cons2 = new ColumnConstraints();
		cons2.setHgrow(Priority.ALWAYS);
		mainArea.getColumnConstraints().addAll(cons1, cons2);

		RowConstraints rcons0 = new RowConstraints();
		rcons0.setVgrow(Priority.ALWAYS);

		RowConstraints rcons1 = new RowConstraints();
		rcons1.setVgrow(Priority.NEVER);

		RowConstraints rcons2 = new RowConstraints();
		rcons2.setVgrow(Priority.ALWAYS);

		mainArea.getRowConstraints().addAll(rcons1, rcons2);

		// on directory button click
		chooseDirectory.setOnAction(e ->
		{
			File selectedDirectory = directoryChooser.showDialog(primaryStage);
			if (selectedDirectory != null)
			{
				assignmentDirectory = selectedDirectory.getAbsolutePath();
				directoryPath.setText(assignmentDirectory);
			}
		});

		about.setOnAction(e ->
		{
			Dialog<?> dialog = new Dialog<>();
			dialog.setTitle("About Copy Catch");
			Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
			dialog.initOwner(primaryStage);
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
			GridPane grid = new GridPane();
			grid.setVgap(8);
			grid.setHgap(8);
			ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/resources/CCIconSmall.png")));
			ImageView iv2 = new ImageView(new Image(getClass().getResourceAsStream("/resources/CCIconSmall.png")));
			iv2.setVisible(false);
			Label name = new Label("Copy Catch v1.0");
			name.setAlignment(Pos.CENTER);
			name.setTextAlignment(TextAlignment.CENTER);
			name.setMaxWidth(Double.MAX_VALUE);
			Label authors = new Label("Authors: Joshua Styger & Brandon Tran");
			authors.setAlignment(Pos.CENTER);
			authors.setTextAlignment(TextAlignment.CENTER);
			authors.setMaxWidth(Double.MAX_VALUE);

			class BorderedTitledPane extends StackPane // NO_UCD (unused code)
			{
				BorderedTitledPane(String titleString, Node content)
				{
					Label title = new Label(" " + titleString + " ");
					title.getStyleClass().add("bordered-titled-title");
					StackPane.setAlignment(title, Pos.TOP_CENTER);

					ScrollPane contentPane = new ScrollPane();
					content.getStyleClass().add("bordered-titled-content");
					//contentPane.getChildren().add(content);
					contentPane.setContent(content);
					contentPane.setPadding(new Insets(10,0,0,0));
					
					getStyleClass().add("bordered-titled-border");
					getChildren().addAll(contentPane, title);
					contentPane.setFitToWidth(true);
				}
			}
			Text tArea = new Text();
			tArea.setTextAlignment(TextAlignment.JUSTIFY);

			InputStream file = getClass().getResourceAsStream("/resources/LICENSE.txt");
			Scanner s = new Scanner(file);
			while (s.hasNextLine())
			{
				tArea.setText(tArea.getText() + s.nextLine() + "\n"); // display the found integer
			}
			s.close();
			tArea.setFocusTraversable(false);
			tArea.setMouseTransparent(true);
			//tArea.positionCaret(0);
			//tArea.setEditable(false);
			//tArea.setScrollTop(0);
			BorderedTitledPane btp = new BorderedTitledPane("Apache 2.0 License", tArea);
			btp.setMaxHeight(500);
			btp.setPrefWidth(460);
			grid.add(iv, 0, 0);
			grid.add(name, 1, 0);
			grid.add(iv2, 2, 0);
			grid.add(authors, 0, 1, 3, 1);
			grid.add(btp, 0, 2, 3, 1);
			dialog.getDialogPane().setContent(grid);			
			stage.show();

		});

		// on run program button click
		runProgram.setOnAction(e ->
		{
			if (directoryPath.getText().equals(""))
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("No Selected Directory");
				alert.setHeaderText(null);
				alert.setContentText("You must select a directory to run the scan on first.");
				alert.showAndWait();
				return;

			}
			else
			{
				Path path = Paths.get(directoryPath.getText());
				if (!Files.exists(path))
				{
					System.out.println("Not a valid path!!!!!");
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Directory Does Not Exist");
					alert.setHeaderText(null);
					alert.setContentText("Please select or enter a valid directory for student files.");
					alert.showAndWait();
					return;
				}

				if (comparisonRan)
				{
					// Ask user if they want to run ANOTHER comparison with dialog
					// Inform them that prior results will be lost.
					// Get buttonclick result and proceed or return
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("New Comparison Confirmation");
					alert.setHeaderText(null);
					alert.setContentText("If you run another comparison, your prior results will be lost. Proceed?");

					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == ButtonType.CANCEL)
					{
						return;
					}
				}
				outputListView.setItems(null);
				fileStats = new ArrayList<FileStats>();
				File dir = new File(assignmentDirectory);
				File[] directoryListing = dir.listFiles();
				ConvertFile.InitializeCPPLists();
				ConvertFile.InitializeCommonKeywordsMap();

				System.out.println(assignmentDirectory);
				// converts each file in directory to generalized file.
				// String str = Long.toHexString(Double.doubleToLongBits(Math.random()));
				validFiles = new ArrayList<>();
				// long start;
				// long end;
				// long total = 0;
				for (File assignment : directoryListing)
				{
					String name = assignment.getName();
					name = name.substring(name.length() - 3, name.length());
					if (name.equals("cpp"))
					{
						validFiles.add(assignment);

						// Implementing FileStats
						fileStats.add(new FileStats(assignment.getName()));
						// start = System.currentTimeMillis();
						ConvertFile.textConverter(assignment, fileStats.get(fileStats.size() - 1));
						// end = System.currentTimeMillis();
						// total += end-start;
					}
				}
				// System.out.println((double)total/1000.0 + " Seconds to complete");
				if (validFiles.size() < 2)
				{
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Not Enought CPP Files Present");
					alert.setHeaderText(null);
					alert.setContentText("There must be at least 2 or more CPP files to run comparisons on.");
					alert.showAndWait();
					return;
				}

				Task<Void> task = new Task<Void>()
				{
					@Override
					public Void call()
					{
						final int totalComparisons = (fileStats.size() * (fileStats.size() - 1)) / 2;
						int completedComparisons = 0;
						FileStats.SetScoresSize(fileStats.size() - 1);
						diff_match_patch dmp = new diff_match_patch();
						long start, end;
						start = System.currentTimeMillis();
						for (int i = 0; i < fileStats.size() - 1; i++)
						{
							for (int j = i + 1; j <= fileStats.size() - 1; j++)
							{
								// TODO: Output consolidation;
								if (fileStats.get(i).GetFileName().contains("stongen")
										&& fileStats.get(j).GetFileName().contains("walsh"))
								{
									System.out.print("");
								}
								String[] strs = FileStats.GetLinesAsStringWithMatchedFunctions(fileStats.get(i),
										fileStats.get(j));

								String str1 = strs[0];
								String str2 = strs[1];

								// String str1 = fileStats.get(i).GetAllLinesAsString();
								// String str2 = fileStats.get(j).GetAllLinesAsString();

								// DIFF IMPLEMENTATION
								LinkedList<diff_match_patch.Diff> diff;
								if (str1.length() > str2.length())
								{
									diff = dmp.diff_main(str1, str2);
								}
								else
								{
									diff = dmp.diff_main(str2, str1);
								}
								dmp.diff_cleanupSemantic(diff);
								// System.out.println(diff);

								// Old calculation
								// int distance = FileComparer.CalculateEditDistance(str1, str2);

								// Diff calculation
								int distance = dmp.diff_levenshtein(diff);
								int bigger = Math.max(str1.length(), str2.length());
								double percent = (bigger - distance) / (double) bigger * 100;
								if (percent < 0)
								{
									percent = 0;
								}

								// double percent = fileStats.get(i).CompareCharCount(fileStats.get(j)) * 100;
								FileStats.scores[i][j - 1] = Double.parseDouble(String.format("%.2f", percent));
								completedComparisons++;
								updateProgress(completedComparisons, totalComparisons);
							}
						}
						end = System.currentTimeMillis();
						System.out.println((end - start) / 1000.0);
						return null;
					}
				};

				task.setOnSucceeded(ep ->
				{
					DisplayResults(Integer.parseInt(similarityTreshhold.getValue().toString()));
				});

				dialogStage = new Stage();
				dialogStage.initStyle(StageStyle.UTILITY);
				dialogStage.setOnCloseRequest(pe ->
				{
					pe.consume();
				});

				dialogStage.setResizable(false);
				dialogStage.initModality(Modality.APPLICATION_MODAL);
				ProgressBar pbar = new ProgressBar();
				progressText = new Label();

				pbar.progressProperty().bind(task.progressProperty());
				pbar.setPrefWidth(300);
				pbar.progressProperty().addListener(new ChangeListener<Number>()
				{
					@Override
					public void changed(ObservableValue<? extends Number> ov, Number t, Number newValue)
					{
						String str = Integer.toString((int) ((double) newValue * 100) + 1) + "%";
						progressText.setText(str);
					}
				});
				BorderPane bp = new BorderPane();
				bp.setPadding(new Insets(10, 10, 10, 10));
				bp.setLeft(pbar);
				bp.setRight(progressText);
				BorderPane.setAlignment(progressText, Pos.CENTER_RIGHT);
				BorderPane.setAlignment(pbar, Pos.CENTER_LEFT);
				Scene scene = new Scene(bp);
				dialogStage.setScene(scene);
				dialogStage.setHeight(100);
				dialogStage.setWidth(400);
				dialogStage.setTitle("Comparing Files...");
				dialogStage.show();
				new Thread(task).start();
				;
			}
		});

		// GridPane.setHalignment(runProgram, HPos.RIGHT);
		// add items to window
		mainArea.add(chooseDirectory, 0, 1);
		mainArea.add(directoryPath, 1, 1);
		mainArea.add(spinnerLabal, 2, 1);
		mainArea.add(similarityTreshhold, 3, 1);
		mainArea.add(runProgram, 4, 1);
		mainArea.add(outputListView, 0, 2, 5, 2);

		root.getChildren().addAll(menubars, mainArea);

		Scene scene = new Scene(root);
		String css = this.getClass().getResource("/resources/MenuBar.css").toExternalForm();
		scene.getStylesheets().add(css);
		primaryStage.setTitle("Copy Catch");
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setMinWidth(primaryStage.getWidth());
		primaryStage.setMinHeight(primaryStage.getHeight());
	}

	private void DisplayResults(int similarityThreshhold)
	{
		comparisonRan = true;
		dialogStage.hide();
		double scores[][] = FileStats.scores;

		List<OutputCell> listResults = new ArrayList<>();
		for (int i = 0; i < scores.length; i++)
		{
			for (int j = i; j < scores.length; j++)
			{
				// Only prints those above given threshold
				if (scores[i][j] >= similarityThreshhold)
				{
					listResults.add(new OutputCell(validFiles.get(i), validFiles.get(j + 1), scores[i][j]));
				}
			}
		}
		Comparator<OutputCell> OutputCellComparater = new Comparator<OutputCell>()
		{
			@Override
			public int compare(OutputCell o1, OutputCell o2)
			{
				if (o1.score > o2.score)
				{
					return -1;
				}
				else if (o1.score < o2.score)
				{
					return 1;
				}
				else
				{
					return 0;
				}
			}
		};
		Collections.sort(listResults, OutputCellComparater);
		if (listResults.size() == 0)
		{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("No matches over threshold");
			alert.setHeaderText(null);
			alert.setContentText(
					"No files equaling or exceeding  " + similarityThreshhold + "% similar elements found.");
			alert.showAndWait();
		}
		ObservableList<OutputCell> observableListResults = FXCollections.observableList(listResults);
		outputListView.setItems(observableListResults);
		System.out.println("Done!");
	}

}
