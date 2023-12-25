package com.example.tankgame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HelloApplication extends Application {

    private static final int GENISLIK = 40;
    private static final int YUKSEKLIK = 60;
    private static final double HAREKET_MIKTARI = 5;
    private static final double ATES_MIKTARI = 7;
    private static final int DUSMAN_SAYISI = 3;
    private static final double DUSMAN_GENISLIK = 30;
    private static final double DUSMAN_YUKSEKLIK = 40;
    private static final Duration DUSMAN_ATES_ARALIGI = Duration.seconds(2);

    private int vurusSayisi = 0;
    private int kalanDusmanSayisi = DUSMAN_SAYISI;

    private List<Rectangle> dusmanlar = new ArrayList<>();
    private List<Timeline> dusmanHareketTimelines = new ArrayList<>();
    private List<Timeline> dusmanAteşTimelines = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane pane = new Pane();

        Rectangle tank = new Rectangle(GENISLIK, YUKSEKLIK, Color.BLUE);
        tank.setTranslateX(180);
        tank.setTranslateY(120);

        // Ana dikdörtgenin hareket ve ateş etme animasyonu
        setupKontrolEdilenDikdortgenAnimasyon(tank, pane);

        pane.getChildren().add(tank);

        // Rastgele hareket eden ve belirli aralıklarla ateş eden 3 dikdörtgen
        for (int i = 0; i < DUSMAN_SAYISI; i++) {
            Rectangle dusman = createDusman();
            dusmanlar.add(dusman);
            pane.getChildren().add(dusman);

            // Dikdörtgenin hareket animasyonu
            Timeline hareketTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(0.016), e -> {
                        dusman.setTranslateX(dusman.getTranslateX() + randomHareket());
                        dusman.setTranslateY(dusman.getTranslateY() + randomHareket());

                        // Eğer düşman ekran dışına çıkarsa, tekrar rastgele bir konuma yerleştir
                        if (dusman.getTranslateX() < 0 || dusman.getTranslateX() > 400 - DUSMAN_GENISLIK ||
                                dusman.getTranslateY() < 0 || dusman.getTranslateY() > 300 - DUSMAN_YUKSEKLIK) {
                            dusman.setTranslateX(random.nextDouble() * (400 - DUSMAN_GENISLIK));
                            dusman.setTranslateY(random.nextDouble() * (300 - DUSMAN_YUKSEKLIK));
                        }
                    })
            );
            hareketTimeline.setCycleCount(Timeline.INDEFINITE);
            hareketTimeline.play();
            dusmanHareketTimelines.add(hareketTimeline);

            // Dikdörtgenin belirli aralıklarla ateş etme animasyonu
            Timeline atesEtmeTimeline = new Timeline(
                    new KeyFrame(DUSMAN_ATES_ARALIGI, ae -> atesEt(pane, dusman.getTranslateX() + DUSMAN_GENISLIK / 2, dusman.getTranslateY()))
            );
            atesEtmeTimeline.setCycleCount(Timeline.INDEFINITE);
            atesEtmeTimeline.play();
            dusmanAteşTimelines.add(atesEtmeTimeline);
        }

        Scene scene = new Scene(pane, 400, 300);

        scene.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            switch (keyCode) {
                case RIGHT:
                    hareketEt(tank, HAREKET_MIKTARI, 0);
                    break;
                case LEFT:
                    hareketEt(tank, -HAREKET_MIKTARI, 0);
                    break;
                case UP:
                    hareketEt(tank, 0, -HAREKET_MIKTARI);
                    break;
                case DOWN:
                    hareketEt(tank, 0, HAREKET_MIKTARI);
                    break;
                case SPACE:
                    atesEt(pane, tank.getTranslateX() + GENISLIK / 2, tank.getTranslateY());
                    break;
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Tank Oyunu");
        primaryStage.show();
    }

    private void setupKontrolEdilenDikdortgenAnimasyon(Rectangle tank, Pane pane) {
        Timeline kontrolEdilenDikdortgenAnimasyon = new Timeline(
                new KeyFrame(Duration.seconds(0.016), e -> {
                    // Kontrol edilen dikdörtgenin hareketi
                    // ...

                    // Ateş etme
                    if (Math.random() < 0.01) {
                        atesEt(pane, tank.getTranslateX() + GENISLIK / 2, tank.getTranslateY());
                    }
                })
        );
        kontrolEdilenDikdortgenAnimasyon.setCycleCount(Timeline.INDEFINITE);
        kontrolEdilenDikdortgenAnimasyon.play();
    }

    private void hareketEt(Rectangle dikdortgen, double deltaX, double deltaY) {
        double yeniX = dikdortgen.getTranslateX() + deltaX;
        double yeniY = dikdortgen.getTranslateY() + deltaY;

        // Sınırları kontrol et
        if (yeniX >= 0 && yeniX <= 400 - dikdortgen.getWidth() && yeniY >= 0 && yeniY <= 300 - dikdortgen.getHeight()) {
            dikdortgen.setTranslateX(yeniX);
            dikdortgen.setTranslateY(yeniY);
        }
    }

    private void atesEt(Pane pane, double x, double y) {
        Rectangle kursun = new Rectangle(3, 10, Color.RED);
        kursun.setTranslateX(x - kursun.getWidth() / 2);
        kursun.setTranslateY(y - kursun.getHeight());

        pane.getChildren().add(kursun);

        // Ateşleme animasyonu
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.016), e -> {
                    kursun.setTranslateY(kursun.getTranslateY() - ATES_MIKTARI);

                    // Kursunun ekran dışına çıkması durumu
                    if (kursun.getTranslateY() < 0) {
                        pane.getChildren().remove(kursun);
                    }

                    // Düşmanları kontrol et ve vuruş durumunu kontrol et
                    Iterator<Rectangle> iterator = dusmanlar.iterator();
                    while (iterator.hasNext()) {
                        Rectangle dusman = iterator.next();
                        if (kursun.getBoundsInParent().intersects(dusman.getBoundsInParent())) {
                            vurusSayisi++;
                            kalanDusmanSayisi--;

                            pane.getChildren().remove(kursun);
                            iterator.remove();
                            dusman.setVisible(false);
                            pane.getChildren().remove(dusman);

                            // Tüm düşmanlar vurulduysa oyunu bitir
                            if (kalanDusmanSayisi == 0) {
                                System.out.println("Oyun Bitti! Kazandınız!");
                                System.exit(0);
                            }
                            break;
                        }
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private Rectangle createDusman() {
        return new Rectangle(DUSMAN_GENISLIK, DUSMAN_YUKSEKLIK, randomRenk());
    }

    private Color randomRenk() {
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private double randomHareket() {
        return (random.nextDouble() - 0.5) * 1 * HAREKET_MIKTARI;
    }

    private Random random = new Random();
}


