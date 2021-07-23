import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

public class SeamCarver {
    private double[][] energy;
    private int[][] rgb;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("Null argument");
        }
        int width = picture.width();
        int height = picture.height();

        // copy rgb pixels
        rgb = new int[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++)
                rgb[row][col] = picture.getRGB(col, row);
        }

        // pre-compute energy
        energy = new double[height][width];
        computeEnergy();
    }

    private void computeEnergy() {
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++)
                energy[row][col] = energy(col, row);
        }
    }

    // current picture
    public Picture picture() {
        // update or process image based on rgb values, that are updated very frequently
        // this way, we can easily update rgb values, without having to also update
        // Picture object, until needed
        Picture pic = new Picture(width(), height());
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                pic.setRGB(col, row, rgb[row][col]);
            }
        }
        return pic;
    }

    // width of current picture
    public int width() {
        return rgb[0].length;
    }

    // height of current picture
    public int height() {
        return rgb.length;
    }

    private boolean pixelWithinWidth(int col) {
        return col >= 0 && col < width();
    }

    private boolean pixelWithinHeight(int row) {
        return row >= 0 && row < height();
    }

    // column x and row y
    private boolean pixelWithinRange(int x, int y) {
        return pixelWithinWidth(x) && pixelWithinHeight(y);
    }

    private boolean isBorderPixel(int x, int y) {
        if (x == 0 || x == width() - 1 || y == 0 || y == height() - 1) {
            return true;
        }
        return false;
    }

    // returns rgb of pixel
    private int rgb(int x, int y, char c) {
        int rgbVal = rgb[y][x];
        switch (c) {
            case 'r':
                return (rgbVal >> 16) & 0xFF;
            case 'g':
                return (rgbVal >> 8) & 0xFF;
            case 'b':
                return (rgbVal >> 0) & 0xFF;
            default:
                throw new IllegalArgumentException("invalid color!");
        }
    }

    private Double centralDiffX(int x, int y) {
        int r_diff = rgb(x + 1, y, 'r') - rgb(x - 1, y, 'r');
        int g_diff = rgb(x + 1, y, 'g') - rgb(x - 1, y, 'g');
        int b_diff = rgb(x + 1, y, 'b') - rgb(x - 1, y, 'b');
        return Math.pow(r_diff, 2) + Math.pow(g_diff, 2) + Math.pow(b_diff, 2);
    }

    private Double centralDiffY(int x, int y) {
        int r_diff = rgb(x, y + 1, 'r') - rgb(x, y - 1, 'r');
        int g_diff = rgb(x, y + 1, 'g') - rgb(x, y - 1, 'g');
        int b_diff = rgb(x, y + 1, 'b') - rgb(x, y - 1, 'b');
        return Math.pow(r_diff, 2) + Math.pow(g_diff, 2) + Math.pow(b_diff, 2);
    }

    private Double centralDiff(int x, int y) {
        Double x_diff = centralDiffX(x, y);
        Double y_diff = centralDiffY(x, y);
        return Math.sqrt(x_diff + y_diff);
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (!pixelWithinRange(x, y)) {
            throw new IllegalArgumentException("Invalid pixel range");
        } else if (isBorderPixel(x, y)) {
            return 1000.0;
        } else {
            return centralDiff(x, y);
        }
    }

    private double[][] transposeMatrix(double[][] arr) {
        double[][] newArr = new double[arr[0].length][arr.length];
        for (int i = 0; i < arr[0].length; i++) {
            for (int j = 0; j < arr.length; j++) {
                newArr[i][j] = arr[j][i];
            }
        }
        return newArr;
    }

    private int[][] transposeMatrix(int[][] arr) {
        int[][] newArr = new int[arr[0].length][arr.length];
        for (int i = 0; i < arr[0].length; i++) {
            for (int j = 0; j < arr.length; j++) {
                newArr[i][j] = arr[j][i];
            }
        }
        return newArr;
    }

    // transposes rgb, and energy calculations
    private void transposeImage() {
        energy = transposeMatrix(energy);
        rgb = transposeMatrix(rgb);
        // computeEnergy();
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        transposeImage();
        int[] seam = findVerticalSeam();
        transposeImage();
        return seam;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        double[][] distTo = new double[height()][width()];
        int[][] edgeTo = new int[height()][width()];
        int[] seam = new int[height()];

        // initialize vertex weights
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                Double val = Double.POSITIVE_INFINITY;
                if (row == 0) {
                    val = energy[row][col];
                }
                distTo[row][col] = val;
            }
        }
        // relax vertices
        for (int row = 0; row < height(); row++) {
            for (int col = 1; col < width() - 1; col++) {
                for (int i = -1; i < 2; ++i) {
                    if (pixelWithinRange(col + i, row + 1)) {
                        if (distTo[row + 1][col + i] > distTo[row][col] + energy[row + 1][col + i]) {
                            // update new lower dist
                            distTo[row + 1][col + i] = distTo[row][col] + energy[row + 1][col + i];
                            // store column index
                            edgeTo[row + 1][col + i] = col;
                        }
                    }
                }
            }
        }
        // backtrack from last row using edgeTo[][]
        int minColIdx = 0;
        for (int col = 1; col < width(); col++) {
            // checking which vertex in the last row has the smallest distTo[]
            if (distTo[height() - 1][col] < distTo[(height() - 1)][minColIdx]) {
                minColIdx = col;
            }
        }

        // build rest of seam
        // seam[k] gives us the desired column idx at row k
        seam[height() - 1] = minColIdx;
        // iterate over remaining rows
        for (int row = height() - 2; row >= 0; --row) {
            seam[row] = edgeTo[row + 1][seam[row + 1]];
        }

        return seam;
    }

    private double[][] cropRows(double[][] src, int[] colsToRemove) {
        int height = colsToRemove.length;
        int width = src[0].length;
        double[][] newArr = new double[height][width - 1];
        for (int row = 0; row < height; row++) {
            System.arraycopy(src[row], 0, newArr[row], 0, colsToRemove[row]);
            System.arraycopy(src[row], colsToRemove[row] + 1, newArr[row], colsToRemove[row],
                    width - colsToRemove[row] - 1);
        }
        return newArr;
    }

    private int[][] cropRows(int[][] src, int[] colsToRemove) {
        int height = colsToRemove.length;
        int width = src[0].length;
        int[][] newArr = new int[height][width - 1];
        for (int row = 0; row < height; row++) {
            System.arraycopy(src[row], 0, newArr[row], 0, colsToRemove[row]);
            System.arraycopy(src[row], colsToRemove[row] + 1, newArr[row], colsToRemove[row],
                    width - colsToRemove[row] - 1);
        }

        return newArr;
    }

    // recalculates only necessary pixels, given a vertical seam
    private void updateEnergy(int[] seam) {
        for (int row = 0; row < seam.length; ++row) {
            int col = seam[row];
            for (int i = -1; i <= 1; ++i) {
                if (pixelWithinRange(col + i, row)) {
                    energy[row][col + i] = energy(col + i, row);
                }
            }
        }
    }

    private void invalidSeam() {
        throw new IllegalArgumentException("Invalid Seam!");
    }

    private void validateSeam(int[] seam, String axis) {
        if (seam == null) {
            invalidSeam();
        }
        switch (axis) {
            case "horizontal":
                if (seam.length != width()) {
                    invalidSeam();
                }
                int prev = seam[0];
                for (int col = 0; col < width(); ++col) {
                    if (!pixelWithinHeight(seam[col]) || Math.abs(seam[col] - prev) > 1) {
                        invalidSeam();
                    }
                    prev = seam[col];
                }
                return;
            case "vertical":
                if (seam.length != height()) {
                    invalidSeam();
                }
                prev = seam[0];
                for (int row = 0; row < height(); ++row) {
                    if (!pixelWithinWidth(seam[row]) || Math.abs(seam[row] - prev) > 1) {
                        invalidSeam();
                    }
                    prev = seam[row];
                }
                return;
            default:
                throw new IllegalArgumentException("invalid direction");
        }
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        validateSeam(seam, "horizontal");
        transposeImage();
        removeVerticalSeam(seam);
        transposeImage();
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        validateSeam(seam, "vertical");
        energy = cropRows(energy, seam);
        rgb = cropRows(rgb, seam);
        // recalculate energy values that are necessary
        updateEnergy(seam);
    }

    // unit testing (optional)
    public static void main(String[] args) {
        Picture p = new Picture(args[0]);
        SeamCarver s = new SeamCarver(p);
        // StdOut.println(s.width());
        // StdOut.println(s.energy(1, 2));
        int[][] matrix = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
        int[][] tMatrix = s.transposeMatrix(matrix);
        for (int i = 0; i < tMatrix.length; ++i) {
            for (int j = 0; j < tMatrix[0].length; ++j) {
                StdOut.print(tMatrix[i][j] + "  ");
            }
            StdOut.print("\n");
        }
    }

}