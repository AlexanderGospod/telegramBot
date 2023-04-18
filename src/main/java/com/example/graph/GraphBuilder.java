package com.example.graph;

import com.example.pojo.AlphaVantageResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.chart.ChartUtils;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class GraphBuilder {
    public static InputFile buildGraph(AlphaVantageResponse response) {
        String CHART_TITLE = "Stock Price";
        String X_AXIS_LABEL = "Date";
        String Y_AXIS_LABEL = "Price";
        // Create a new time series collection to hold the stock price data
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        // Create a new time series object to hold the stock prices
        TimeSeries prices = new TimeSeries("Prices");

        // Loop over the data in the response and add it to the time series
        for (Map.Entry<String, AlphaVantageResponse.DataOnTheValueOfTheShares> entry : response.getDataOnTheValueOfTheShares().entrySet()) {
            String dateString = entry.getKey();
            AlphaVantageResponse.DataOnTheValueOfTheShares data = entry.getValue();
            double price = Double.parseDouble(data.getClose());
            Day day = new Day(Integer.parseInt(dateString.substring(8, 10)), Integer.parseInt(dateString.substring(5, 7)), Integer.parseInt(dateString.substring(0, 4)));
            prices.add(day, price);
        }

        // Add the time series to the dataset
        dataset.addSeries(prices);

        // Create a new chart object
        JFreeChart chart = ChartFactory.createTimeSeriesChart(CHART_TITLE, X_AXIS_LABEL, Y_AXIS_LABEL, dataset);

        // Customize the chart
        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5, 5));
        plot.setRenderer(renderer);

        // Add annotations to the chart to mark the split coefficients
        for (Map.Entry<String, AlphaVantageResponse.DataOnTheValueOfTheShares> entry : response.getDataOnTheValueOfTheShares().entrySet()) {
            String dateString = entry.getKey();
            AlphaVantageResponse.DataOnTheValueOfTheShares data = entry.getValue();
            double splitCoefficient = Double.parseDouble(data.getSplitCoefficient());
            if (splitCoefficient != 1.0) {
                Day day = new Day(Integer.parseInt(dateString.substring(8, 10)), Integer.parseInt(dateString.substring(5, 7)), Integer.parseInt(dateString.substring(0, 4)));
                XYTextAnnotation annotation = new XYTextAnnotation("split = " + String.valueOf(splitCoefficient), day.getFirstMillisecond(), plot.getRangeAxis().getUpperBound());
                annotation.setFont(annotation.getFont().deriveFont(12.0f));
                plot.addAnnotation(annotation);
            }
        }
        // Set upper margin to create more space above the graph
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setUpperMargin(0.1);
        return convertJFreeChartObjectToImage(chart);
    }

    private static InputFile convertJFreeChartObjectToImage(JFreeChart chart){
        int WIDTH = 1600;
        int HEIGHT = 900;
        // Save the chart as an image
        File outputFile = new File("target/priceGraph");
        try {
            ChartUtils.saveChartAsJPEG(outputFile, chart, WIDTH, HEIGHT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Load the image file into a BufferedImage object
        BufferedImage image = null;
        try {
            image = ImageIO.read(outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Convert the BufferedImage object to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] imageBytes = outputStream.toByteArray();

        // Create a new InputFile object from the byte array
        InputFile photo = new InputFile(new ByteArrayInputStream(imageBytes), "chart.png");
        return photo;
    }
}
