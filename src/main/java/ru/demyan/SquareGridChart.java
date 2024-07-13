package ru.demyan;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SquareGridChart extends ApplicationFrame {

    public SquareGridChart(String title, List<Square> squares) {
        super(title);
        JFreeChart chart = createChart(squares);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private JFreeChart createChart(List<Square> squares) {
        DefaultXYDataset dataset = new DefaultXYDataset();

        // Adding data points for each square
        for (Square square : squares) {
            double[][] data = {{square.getX(), square.getX() + square.getSize()},
                    {square.getY(), square.getY() + square.getSize()}};
            dataset.addSeries(square.toString(), data);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Square Grid Chart",
                "X Axis",
                "Y Axis",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        XYShapeRenderer renderer = new XYShapeRenderer();
        plot.setRenderer(renderer);

        // Setting up colors and shapes for the squares
        for (int i = 0; i < squares.size(); i++) {
            Square square = squares.get(i);
            renderer.setSeriesPaint(i, Color.BLUE);  // Set color of squares
            renderer.setSeriesShape(i, new Rectangle(square.getSize(), square.getSize()));  // Set shape of squares
        }

        return chart;
    }

    public static void main(String[] args) {
        List<Square> squares = generateSquares(0, 0, 25, 25, 10);  // Example: Generate 5x5 grid of squares

        SquareGridChart chart = new SquareGridChart("Square Grid Chart Example", squares);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }

    private static List<Square> generateSquares(int startX, int startY, int size, int width, int height) {
        List<Square> squares = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                squares.add(new Square(startX + i * size, startY + j * size, size));
            }
        }
        return squares;
    }
}

// Class to represent a square
class Square {
    private int x;
    private int y;
    private int size;

    public Square(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
