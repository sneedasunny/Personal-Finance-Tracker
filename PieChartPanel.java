import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class PieChartPanel extends JPanel {
    private Map<String, Double> data = null;

    public void setData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (data == null || data.isEmpty()) {
            g.setFont(g.getFont().deriveFont(Font.ITALIC, 14f));
            g.drawString("No expense data to show", 20, 40);
            return;
        }

        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h) - 40;
        int x = (w - size) / 2;
        int y = 10;

        double total = 0;
        for (double v : data.values())
            total += v;
        if (total <= 0)
            return;

        // Choose colors (deterministic but varied)
        Color[] palette = new Color[] {
                new Color(0x4E79A7), new Color(0xF28E2B), new Color(0xE15759),
                new Color(0x76B7B2), new Color(0x59A14F), new Color(0xEDC948),
                new Color(0xB07AA1), new Color(0xFF9DA7), new Color(0x9C755F), new Color(0xBAB0AC)
        };

        double angle = 0;
        int i = 0;
        int legendX = 10;
        int legendY = y + size + 20;
        int legendH = 16;
        int legendGap = 6;

        // draw slices
        int idx = 0;
        for (Map.Entry<String, Double> e : data.entrySet()) {
            double v = e.getValue();
            double ang = v / total * 360.0;
            g.setColor(palette[idx % palette.length]);
            g.fillArc(x, y, size, size, (int) Math.round(angle), (int) Math.round(ang));
            angle += ang;
            idx++;
        }

        // draw inner white circle for donut look
        int inner = (int) (size * 0.45);
        int ix = x + (size - inner) / 2;
        int iy = y + (size - inner) / 2;
        g.setColor(getBackground());
        g.fillOval(ix, iy, inner, inner);

        // draw legend
        idx = 0;
        int ly = legendY;
        g.setFont(g.getFont().deriveFont(12f));
        for (Map.Entry<String, Double> e : data.entrySet()) {
            String k = e.getKey();
            double v = e.getValue();
            String text = String.format("%s — ₹%.2f (%.0f%%)", k, v, (v / total * 100));
            g.setColor(palette[idx % palette.length]);
            g.fillRect(legendX, ly, legendH, legendH);
            g.setColor(Color.BLACK);
            g.drawString(text, legendX + legendH + legendGap, ly + legendH - 3);
            ly += legendH + 6;
            idx++;
        }
    }
}
