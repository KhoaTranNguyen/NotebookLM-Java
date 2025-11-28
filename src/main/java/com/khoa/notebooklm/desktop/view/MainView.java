package com.khoa.notebooklm.desktop.view;

import com.khoa.notebooklm.desktop.controller.RAGController;
import com.khoa.notebooklm.desktop.model.Flashcard;
import com.khoa.notebooklm.desktop.model.dao.DocumentDao;
import com.khoa.notebooklm.desktop.model.dao.FlashcardDao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainView {

    private final BorderPane root = new BorderPane();
    private final RAGController ragController = new RAGController();
    private final DocumentDao docDao = new DocumentDao();
    private final FlashcardDao flashcardDao = new FlashcardDao();
    
    // State for Flashcard Player
    private List<Flashcard> currentFlashcardSet = new ArrayList<>();
    private int currentCardIndex = 0;
    private boolean isShowingFront = true;
    private Label cardContentLabel;
    private Label cardCounterLabel;

    public MainView(long userId) {
        // Top bar
        HBox top = new HBox();
        top.getStyleClass().addAll("cds-section");
        top.setPadding(new Insets(8, 16, 8, 16));
        Label title = new Label("NotebookLM Desktop");
        title.getStyleClass().add("cds-title");
        top.getChildren().addAll(title);

        // Sidebar
        VBox sideBox = new VBox(24); // Increased spacing between sections
        sideBox.setMinWidth(300); // Wider sidebar for contained lists
        sideBox.setPrefWidth(300);
        sideBox.setMaxWidth(300);
        // sideBox.getStyleClass().add("cds-section"); // Removed to avoid extra spacing issues
        // sideBox.setPadding(new Insets(16)); // Removed padding to align with tabs

        // 1. Document List (Contained List Pattern)
        VBox docContainer = new VBox();
        docContainer.getStyleClass().add("contained-list");
        VBox.setVgrow(docContainer, Priority.ALWAYS); // Grow to fill height
        
        HBox docHeader = new HBox();
        docHeader.getStyleClass().add("contained-list-header");
        docHeader.setAlignment(Pos.CENTER_LEFT);
        Label docTitle = new Label("Documents");
        docTitle.setStyle("-fx-font-weight: bold;");
        Region docSpacer = new Region();
        HBox.setHgrow(docSpacer, Priority.ALWAYS);
        
        Button uploadBtn = new Button("Upload");
        uploadBtn.getStyleClass().add("secondary");
        uploadBtn.setStyle("-fx-font-size: 12px; -fx-padding: 5 10;"); // Compact button
        
        docHeader.getChildren().addAll(docTitle, docSpacer, uploadBtn);

        ListView<DocumentDao.DocumentRow> docList = new ListView<>();
        docList.getItems().addAll(docDao.listDocumentsByUserId(userId));
        docList.setCellFactory(list -> new ListCell<>() {
            private final Button deleteBtn = new Button();
            private final Label label = new Label();
            private final HBox pane = new HBox(10, label, deleteBtn);

            {
                FontIcon deleteIcon = new FontIcon("fas-times");
                deleteIcon.setIconColor(javafx.scene.paint.Color.BLACK);
                deleteBtn.setGraphic(deleteIcon);
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setOnAction(event -> {
                    DocumentDao.DocumentRow item = getItem();
                    if (item != null) {
                        try {
                            ragController.deleteDocument(item.id());
                            getListView().getItems().remove(item);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                HBox.setHgrow(label, Priority.ALWAYS);
                label.setMaxWidth(Double.MAX_VALUE);
                pane.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(DocumentDao.DocumentRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    label.setText(item.filename());
                    setText(null);
                    setGraphic(pane);
                }
            }
        });
        VBox.setVgrow(docList, Priority.ALWAYS);
        docContainer.getChildren().addAll(docHeader, docList);

        final long[] selectedDocId = { -1 };
        docList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selectedDocId[0] = (newV == null ? -1 : newV.id());
        });

        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select PDF Document");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showOpenDialog(root.getScene().getWindow());
            if (file != null) {
                try {
                    int newDocId = ragController.ingestDocument(userId, file);
                    docList.getItems().clear();
                    docList.getItems().addAll(docDao.listDocumentsByUserId(userId));

                    // Auto-select the newly uploaded document if present
                    for (int i = 0; i < docList.getItems().size(); i++) {
                        DocumentDao.DocumentRow row = docList.getItems().get(i);
                        if (row.id() == newDocId) {
                            docList.getSelectionModel().select(i);
                            docList.scrollTo(i);
                            selectedDocId[0] = newDocId;
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 2. Saved Flashcards List (Contained List Pattern)
        VBox fcSetContainer = new VBox();
        fcSetContainer.getStyleClass().add("contained-list");
        VBox.setVgrow(fcSetContainer, Priority.ALWAYS); // Grow to fill height

        HBox fcHeader = new HBox();
        fcHeader.getStyleClass().add("contained-list-header");
        fcHeader.setAlignment(Pos.CENTER_LEFT);
        Label fcTitleLabel = new Label("Saved Flashcards");
        fcTitleLabel.setStyle("-fx-font-weight: bold;");
        fcHeader.getChildren().add(fcTitleLabel);

        ListView<FlashcardDao.FlashcardSetInfo> fcSetList = new ListView<>();
        // Load initial sets for this user
        fcSetList.getItems().addAll(flashcardDao.getFlashcardSetsByUserId(userId));
        
        fcSetList.setCellFactory(list -> new ListCell<>() {
            private final Button deleteBtn = new Button();
            private final Label label = new Label();
            private final HBox pane = new HBox(10, label, deleteBtn);
            {
                FontIcon deleteIcon = new FontIcon("fas-times");
                deleteIcon.setIconColor(javafx.scene.paint.Color.BLACK);
                deleteBtn.setGraphic(deleteIcon);
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setOnAction(event -> {
                    FlashcardDao.FlashcardSetInfo item = getItem();
                    if (item != null) {
                        flashcardDao.deleteFlashcardSet(item.id());
                        getListView().getItems().remove(item);
                    }
                });
                HBox.setHgrow(label, Priority.ALWAYS);
                label.setMaxWidth(Double.MAX_VALUE);
                pane.setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            protected void updateItem(FlashcardDao.FlashcardSetInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    label.setText(item.topic());
                    setText(null);
                    setGraphic(pane);
                }
            }
        });
        VBox.setVgrow(fcSetList, Priority.ALWAYS);
        fcSetContainer.getChildren().addAll(fcHeader, fcSetList);

        sideBox.getChildren().addAll(docContainer, fcSetContainer);


        // Center content: Tabs for Chat and Flashcards
        TabPane tabs = new TabPane();
        Tab chatTab = new Tab("Chat AI");
        Tab fcTab = new Tab("Flashcards");
        chatTab.setClosable(false);
        fcTab.setClosable(false);

        // Chat content
        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));
        chatBox.getStyleClass().addAll("cds-card", "cds-section");
        Label centerTitle = new Label("Chat about your document:");
        TextArea prompt = new TextArea();
        prompt.setPromptText("Ask something...");
        prompt.setPrefRowCount(3);
        Button ask = new Button("Ask AI");
        // ask is Primary by default
        TextArea answer = new TextArea();
        answer.setEditable(false);
        answer.setWrapText(true);
        VBox.setVgrow(answer, Priority.ALWAYS);
        
        chatBox.getChildren().addAll(centerTitle, prompt, ask, answer);
        chatTab.setContent(chatBox);

        // Flashcards content (Generator + Player)
        VBox fcContainer = new VBox(10);
        fcContainer.setPadding(new Insets(10));
        fcContainer.getStyleClass().addAll("cds-card", "cds-section");

        // -- Generator View --
        VBox generatorView = new VBox(20);
        generatorView.setAlignment(Pos.CENTER);
        
        Label fcTitle = new Label("Get flashcards based on your documents for better memorization!");
        fcTitle.setStyle("-fx-font-size: 20px;");
        
        Button genBtn = new Button("Generate flashcards");
        // genBtn is Primary by default
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #da1e28;"); // Carbon Red 60
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        generatorView.getChildren().addAll(fcTitle, genBtn, errorLabel);

        // -- Player View --
        VBox playerView = new VBox(20);
        playerView.setAlignment(Pos.CENTER);
        playerView.setVisible(false);
        playerView.setManaged(false); // Hide initially

        Label topicLabel = new Label("Topic");
        topicLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        StackPane cardPane = new StackPane();
        cardPane.setPrefSize(400, 250);
        cardPane.getStyleClass().add("flashcard-pane");
        
        cardContentLabel = new Label("Front Content");
        cardContentLabel.setWrapText(true);
        cardContentLabel.setStyle("-fx-font-size: 16px;");
        cardContentLabel.setPadding(new Insets(20));
        cardPane.getChildren().add(cardContentLabel);

        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);
        Button prevCard = new Button("< Prev");
        prevCard.getStyleClass().add("secondary");
        Button flipCard = new Button("Flip");
        // flipCard is Primary by default
        Button nextCard = new Button("Next >");
        nextCard.getStyleClass().add("secondary");
        controls.getChildren().addAll(prevCard, flipCard, nextCard);

        cardCounterLabel = new Label("0 / 0");
        Button backToGen = new Button("Back to Generator");
        backToGen.getStyleClass().add("secondary");
        Button saveCurrentSetBtn = new Button("Save This Set");
        saveCurrentSetBtn.getStyleClass().add("secondary");

        playerView.getChildren().addAll(backToGen, topicLabel, cardPane, controls, cardCounterLabel, saveCurrentSetBtn);

        fcContainer.getChildren().addAll(generatorView, playerView);
        fcTab.setContent(fcContainer);

        tabs.getTabs().addAll(chatTab, fcTab);

        // --- Event Handlers ---

        // Chat
        ask.setOnAction(e -> {
            try {
                String res;
                if (selectedDocId[0] > 0) {
                    res = ragController.chatWithDocument(selectedDocId[0], prompt.getText());
                } else {
                    res = ragController.chat(prompt.getText());
                }
                answer.setText(res);
            } catch (Exception ex) {
                answer.setText("LLM error: " + ex.getMessage());
            }
        });

        // Flashcard Generation
        genBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            try {
                if (selectedDocId[0] <= 0) {
                    errorLabel.setText("Please select a document from the list first.");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    return;
                }
                // Generate set with auto-title
                RAGController.GeneratedFlashcardSet set = ragController.generateFlashcardSet(selectedDocId[0], 5);
                
                // Switch to player
                generatorView.setVisible(false);
                generatorView.setManaged(false);
                playerView.setVisible(true);
                playerView.setManaged(true);
                saveCurrentSetBtn.setVisible(true);
                saveCurrentSetBtn.setManaged(true);
                
                topicLabel.setText(set.title());
                currentFlashcardSet = set.cards();
                currentCardIndex = 0;
                isShowingFront = true;
                updateCardDisplay();
                
            } catch (Exception ex) {
                errorLabel.setText("Error: " + ex.getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        });

        // Save Set (from Player View)
        saveCurrentSetBtn.setOnAction(e -> {
            if (currentFlashcardSet.isEmpty()) return;
            
            TextInputDialog dialog = new TextInputDialog(topicLabel.getText());
            dialog.setTitle("Save Flashcard Set");
            dialog.setHeaderText("Confirm topic name:");
            Optional<String> result = dialog.showAndWait();
            
            result.ifPresent(topic -> {
                try {
                    flashcardDao.saveFlashcardSet(userId, topic, currentFlashcardSet);
                    fcSetList.getItems().clear();
                    fcSetList.getItems().addAll(flashcardDao.getFlashcardSetsByUserId(userId));
                    
                    // Hide save button after saving
                    saveCurrentSetBtn.setVisible(false);
                    saveCurrentSetBtn.setManaged(false);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Set saved successfully!");
                    alert.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        // Select Saved Set -> Switch to Player
        fcSetList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                tabs.getSelectionModel().select(fcTab);
                generatorView.setVisible(false);
                generatorView.setManaged(false);
                playerView.setVisible(true);
                playerView.setManaged(true);
                
                // Hide save button for saved sets
                saveCurrentSetBtn.setVisible(false);
                saveCurrentSetBtn.setManaged(false);

                topicLabel.setText(newV.topic());
                currentFlashcardSet = flashcardDao.getFlashcardsBySetId(newV.id());
                currentCardIndex = 0;
                isShowingFront = true;
                updateCardDisplay();
            }
        });

        // Player Controls
        backToGen.setOnAction(e -> {
            playerView.setVisible(false);
            playerView.setManaged(false);
            generatorView.setVisible(true);
            generatorView.setManaged(true);
            fcSetList.getSelectionModel().clearSelection();
        });

        flipCard.setOnAction(e -> {
            isShowingFront = !isShowingFront;
            updateCardDisplay();
        });

        prevCard.setOnAction(e -> {
            if (currentCardIndex > 0) {
                currentCardIndex--;
                isShowingFront = true;
                updateCardDisplay();
            }
        });

        nextCard.setOnAction(e -> {
            if (currentCardIndex < currentFlashcardSet.size() - 1) {
                currentCardIndex++;
                isShowingFront = true;
                updateCardDisplay();
            }
        });

        // Footer
        VBox footer = new VBox();
        footer.setPadding(new Insets(32, 16, 16, 16)); // Top 32px margin from content above
        Button logout = new Button("Logout");
        logout.getStyleClass().add("secondary");
        logout.setPrefWidth(300); // Match sidebar width
        logout.setOnAction(e -> root.getScene().setRoot(new LoginView().getRoot()));
        footer.getChildren().add(logout);

        root.setTop(top);
        
        // Main Body Container (Sidebar + Tabs)
        HBox mainBody = new HBox(32); // 32px gap between sidebar and tabs
        mainBody.setPadding(new Insets(16)); // 16px padding around the whole body
        mainBody.getChildren().addAll(sideBox, tabs);
        HBox.setHgrow(tabs, Priority.ALWAYS);
        
        root.setCenter(mainBody);
        root.setBottom(footer);
    }

    private void updateCardDisplay() {
        if (currentFlashcardSet.isEmpty()) {
            cardContentLabel.setText("No cards in this set.");
            cardCounterLabel.setText("0 / 0");
            return;
        }
        Flashcard card = currentFlashcardSet.get(currentCardIndex);
        cardContentLabel.setText(isShowingFront ? card.getFront() : card.getBack());
        cardCounterLabel.setText((currentCardIndex + 1) + " / " + currentFlashcardSet.size());
    }

    public Parent getRoot() {
        return root;
    }
}
