package com.runescape.entity.model;

import java.util.ArrayList;
import java.util.List;

/**
 * RELOAD's own models. They are not in the cache: every model is built
 * here from simple shapes, so they exist in no other game or server.
 *
 * Model ids: 70000 + (tier - 1) * 10 + kind
 *   kind 0 = weapon as worn, 1 = weapon in the inventory,
 *        2 = shield as worn, 3 = shield in the inventory
 * The owner set uses 70060-70065: the same four kinds plus
 *   kind 4 = crown as worn, 5 = crown in the inventory
 *
 * Shapes are drawn in their own flat space: u runs along the length
 * (from the grip towards the tip), w across the width, t is thickness.
 * A "pose" then places that space in the hand or flat for the inventory.
 */
public final class CustomModels {

    public static final int BASE = 70000;
    private static final int TIERS = 6;

    /** Tier colours: hue (0-63) and saturation (0-7), same as the items. */
    private static final int[] HUE = {5, 33, 21, 50, 0, 9};
    private static final int[] SAT = {5, 6, 7, 6, 7, 7};

    private CustomModels() {
    }

    /** The owner set comes after the tiers. */
    private static final int OWNER = TIERS;

    public static boolean isCustom(int id) {
        if (id < BASE || id >= BASE + (TIERS + 1) * 10) {
            return false;
        }
        int kind = (id - BASE) % 10;
        return (id - BASE) / 10 == OWNER ? kind < 6 : kind < 4;
    }

    public static Model build(int id) {
        int tier = (id - BASE) / 10;
        int kind = (id - BASE) % 10;
        if (tier == OWNER) {
            return owner(kind);
        }
        boolean weapon = kind < 2;
        boolean worn = kind == 0 || kind == 2;
        Builder b = new Builder(weapon ? (worn ? Pose.WEAPON_WORN : Pose.WEAPON_INVENTORY)
                : (worn ? Pose.SHIELD_WORN : Pose.SHIELD_INVENTORY));
        if (weapon) {
            weapon(b, tier);
        } else {
            shield(b, tier);
        }
        if (!worn) {
            return b.toModel(-1, -1, 10);
        }
        // Labels that make the model follow the right hand / left arm.
        return weapon ? b.toModel(50, 29, 10) : b.toModel(28, 16, 10);
    }

    // ---- colours ----------------------------------------------------------

    private static int hsl(int hue, int saturation, int lightness) {
        return (hue << 10) | (saturation << 7) | lightness;
    }

    private static int tierColour(int tier, int lightness) {
        return hsl(HUE[tier], SAT[tier], lightness);
    }

    private static final int STEEL_DARK = hsl(0, 0, 30);
    private static final int STEEL = hsl(0, 0, 55);
    private static final int STEEL_LIGHT = hsl(0, 0, 85);
    private static final int LEATHER = hsl(6, 3, 28);
    private static final int GOLD = hsl(9, 7, 70);

    // ---- owner set --------------------------------------------------------

    private static final int WHITE = hsl(0, 0, 112);
    private static final int BLACK = hsl(0, 0, 12);
    private static final int RUBY = hsl(0, 7, 60);

    private static Model owner(int kind) {
        switch (kind) {
            case 0:
            case 1: { // the owner's blade: broad white steel, a crown for a guard
                Builder b = new Builder(kind == 0 ? Pose.WEAPON_WORN : Pose.WEAPON_INVENTORY);
                b.box(-24, 2, -3, 3, 7, BLACK);
                b.plate(circle(-30, 0, 7, 6), 9, RUBY);
                b.plate(new int[][]{{2, -20}, {10, -20}, {20, -26}, {14, -12}, {24, -6}, {14, 0}, {24, 6},
                        {14, 12}, {20, 26}, {10, 20}, {2, 20}}, 8, GOLD);
                b.plate(new int[][]{{12, -10}, {112, -12}, {146, 0}, {112, 12}, {12, 10}}, 4, WHITE);
                b.plate(new int[][]{{14, -3}, {120, -3}, {132, 0}, {120, 3}, {14, 3}}, 6, GOLD);
                b.plate(circle(8, 0, 5, 6), 11, RUBY);
                return kind == 0 ? b.toModel(50, 29, 10) : b.toModel(-1, -1, 10);
            }
            case 2:
            case 3: { // the owner's shield: black and gold with a crown
                Builder b = new Builder(kind == 2 ? Pose.SHIELD_WORN : Pose.SHIELD_INVENTORY);
                b.flipWidth = true;
                b.plate(new int[][]{{-38, 44}, {38, 44}, {38, 0}, {24, -32}, {0, -50}, {-24, -32}, {-38, 0}}, 6,
                        GOLD);
                b.plate(new int[][]{{-32, 38}, {32, 38}, {32, 0}, {20, -28}, {0, -42}, {-20, -28}, {-32, 0}}, 8,
                        BLACK);
                b.plate(new int[][]{{-20, 0}, {20, 0}, {24, 26}, {12, 12}, {0, 30}, {-12, 12}, {-24, 26}}, 11,
                        GOLD);
                b.box(-20, 20, -8, 0, 11, GOLD);
                b.plate(circle(0, -4, 4, 6), 13, RUBY);
                b.plate(circle(0, -24, 5, 6), 11, WHITE);
                return kind == 2 ? b.toModel(28, 16, 10) : b.toModel(-1, -1, 10);
            }
            default: { // the crown
                Builder b = new Builder(kind == 4 ? Pose.CROWN_WORN : Pose.CROWN_INVENTORY);
                b.crown(13, 9, 12, 8, GOLD, hsl(9, 7, 48));
                // A ruby on every other point.
                for (int i = 0; i < 8; i += 2) {
                    double a = 2 * Math.PI * i / 8;
                    b.spike((int) Math.round(14 * Math.cos(a)), (int) Math.round(14 * Math.sin(a)), 3, 8, 2,
                            RUBY);
                }
                return kind == 4 ? b.toModel(1, 0, 8) : b.toModel(-1, -1, 8);
            }
        }
    }

    // ---- weapons ----------------------------------------------------------

    private static void weapon(Builder b, int tier) {
        int bright = tierColour(tier, 80);
        int mid = tierColour(tier, 58);
        int dark = tierColour(tier, 36);
        switch (tier) {
            case 0: // Gloed: a flame blade with wavy edges
                hilt(b, 16, 22, mid, GOLD);
                b.plate(new int[][]{{8, -7}, {26, -11}, {44, -6}, {62, -11}, {80, -6}, {98, -9}, {124, 0},
                        {98, 9}, {80, 6}, {62, 11}, {44, 6}, {26, 11}, {8, 7}}, 3, bright);
                b.plate(new int[][]{{8, -2}, {110, -1}, {110, 1}, {8, 2}}, 5, dark);
                break;
            case 1: // Vorst: a long crystal blade with ice shards at the guard
                hilt(b, 18, 26, mid, STEEL_LIGHT);
                b.plate(new int[][]{{8, -6}, {100, -9}, {132, 0}, {100, 9}, {8, 6}}, 4, bright);
                b.plate(new int[][]{{8, -2}, {118, -2}, {126, 0}, {118, 2}, {8, 2}}, 6, hsl(HUE[1], 3, 110));
                b.plate(new int[][]{{6, -13}, {30, -22}, {16, -9}}, 3, mid);
                b.plate(new int[][]{{6, 13}, {30, 22}, {16, 9}}, 3, mid);
                break;
            case 2: // Gif: a war hammer with a fang on the back
                b.box(-22, 96, -3, 3, 6, LEATHER);
                b.box(-26, -22, -5, 5, 10, STEEL_DARK);
                b.box(84, 116, -22, 14, 22, mid);
                b.box(88, 112, -27, -22, 26, dark);
                b.plate(new int[][]{{90, 14}, {100, 40}, {110, 14}}, 8, bright);
                b.box(116, 122, -6, 6, 10, STEEL);
                break;
            case 3: // Schaduw: a morning star, eight spikes on a dark head
                b.box(-22, 84, -3, 3, 6, STEEL_DARK);
                b.box(-26, -22, -5, 5, 10, mid);
                b.box(84, 116, -16, 16, 32, dark);
                b.plate(new int[][]{{92, 16}, {100, 36}, {108, 16}}, 6, bright);
                b.plate(new int[][]{{92, -16}, {100, -36}, {108, -16}}, 6, bright);
                b.plate(new int[][]{{116, -8}, {138, 0}, {116, 8}}, 6, bright);
                b.spike(100, 0, 16, 34, 8, bright);
                b.spike(100, 0, -16, -34, 8, bright);
                break;
            case 4: // Reload: a needle-thin duelling blade with a swept guard
                b.box(-20, 0, -2, 2, 5, LEATHER);
                b.box(-25, -20, -4, 4, 8, bright);
                b.plate(new int[][]{{0, -16}, {6, -18}, {22, -5}, {22, 5}, {6, 18}, {0, 16}}, 4, mid);
                b.plate(new int[][]{{-16, 12}, {0, 18}, {4, 14}, {-14, 8}}, 3, mid);
                b.plate(new int[][]{{20, -3}, {120, -2}, {140, 0}, {120, 2}, {20, 3}}, 3, STEEL_LIGHT);
                b.plate(new int[][]{{20, -1}, {96, -1}, {96, 1}, {20, 1}}, 5, bright);
                break;
            default: // Goud: a butcher's cleaver, the tool of the trade
                b.flipWidth = true; // the cutting edge faces forward
                b.box(-24, 4, -4, 4, 8, LEATHER);
                b.box(-28, -24, -5, 5, 10, GOLD);
                b.box(4, 10, -7, 7, 10, GOLD);
                b.plate(new int[][]{{10, -8}, {104, -8}, {112, 0}, {112, 34}, {96, 44}, {24, 44}, {10, 30}}, 4,
                        bright);
                b.plate(new int[][]{{14, 30}, {26, 41}, {94, 41}, {108, 32}, {108, 36}, {95, 44}, {25, 44},
                        {12, 33}}, 5, hsl(9, 4, 112));
                b.plate(new int[][]{{92, -4}, {100, -4}, {100, 4}, {92, 4}}, 6, dark);
                break;
        }
    }

    /** Grip, pommel and a straight cross guard. */
    private static void hilt(Builder b, int guardHalfWidth, int gripLength, int guardColour, int pommelColour) {
        b.box(-gripLength, 2, -3, 3, 6, LEATHER);
        b.box(-gripLength - 6, -gripLength, -5, 5, 9, pommelColour);
        b.box(2, 8, -guardHalfWidth, guardHalfWidth, 8, guardColour);
    }

    // ---- shields ----------------------------------------------------------

    private static void shield(Builder b, int tier) {
        int bright = tierColour(tier, 80);
        int mid = tierColour(tier, 58);
        int dark = tierColour(tier, 36);
        switch (tier) {
            case 0: // Gloed: a round buckler with a sun boss
                b.plate(circle(0, 0, 38, 12), 6, mid);
                b.plate(circle(0, 0, 30, 12), 8, dark);
                b.plate(star(0, 0, 22, 9, 8), 11, bright);
                break;
            case 1: // Vorst: a six-sided ice crystal
                b.plate(circle(0, 0, 42, 6), 6, mid);
                b.plate(circle(0, 0, 30, 6), 9, bright);
                b.plate(new int[][]{{-26, 0}, {0, -6}, {26, 0}, {0, 6}}, 12, hsl(HUE[1], 3, 112));
                b.plate(new int[][]{{0, -26}, {6, 0}, {0, 26}, {-6, 0}}, 12, hsl(HUE[1], 3, 112));
                break;
            case 2: // Gif: a fanged triangle
                b.flipWidth = true; // point down
                b.plate(new int[][]{{-40, 34}, {40, 34}, {0, -46}}, 6, mid);
                b.plate(new int[][]{{-26, 24}, {26, 24}, {0, -28}}, 9, dark);
                b.plate(new int[][]{{-14, 20}, {-4, 20}, {-9, -6}}, 12, bright);
                b.plate(new int[][]{{4, 20}, {14, 20}, {9, -6}}, 12, bright);
                break;
            case 3: // Schaduw: a crescent moon on a dark kite
                b.flipWidth = true; // point down
                b.plate(new int[][]{{-32, 40}, {32, 40}, {36, 6}, {0, -48}, {-36, 6}}, 6, dark);
                b.plate(new int[][]{{-18, 26}, {-4, 30}, {8, 22}, {14, 8}, {8, -6}, {-4, -14}, {-18, -10},
                        {-8, -4}, {-2, 8}, {-8, 20}}, 10, bright);
                break;
            case 4: // Reload: a tower shield with the letter R
                b.flipWidth = true; // point down, letter upright
                b.plate(new int[][]{{-32, 44}, {32, 44}, {32, -34}, {0, -48}, {-32, -34}}, 6, mid);
                b.plate(new int[][]{{-26, 38}, {26, 38}, {26, -30}, {0, -41}, {-26, -30}}, 8, dark);
                b.plate(new int[][]{{-14, 28}, {-6, 28}, {-6, -26}, {-14, -26}}, 11, bright);
                b.plate(new int[][]{{-6, 28}, {10, 28}, {16, 20}, {16, 10}, {10, 2}, {-6, 2}, {-6, 9}, {6, 9},
                        {9, 12}, {9, 18}, {6, 21}, {-6, 21}}, 11, bright);
                b.plate(new int[][]{{-2, 2}, {6, 2}, {18, -26}, {10, -26}}, 11, bright);
                break;
            default: // Goud: a sun disc with twelve rays
                b.plate(star(0, 0, 46, 30, 12), 5, GOLD);
                b.plate(circle(0, 0, 30, 12), 8, bright);
                b.plate(circle(0, 0, 18, 12), 11, hsl(9, 4, 112));
                break;
        }
    }

    private static int[][] circle(int cu, int cw, int radius, int points) {
        int[][] p = new int[points][2];
        for (int i = 0; i < points; i++) {
            double a = 2 * Math.PI * i / points;
            p[i][0] = cu + (int) Math.round(radius * Math.cos(a));
            p[i][1] = cw + (int) Math.round(radius * Math.sin(a));
        }
        return p;
    }

    private static int[][] star(int cu, int cw, int outer, int inner, int points) {
        int[][] p = new int[points * 2][2];
        for (int i = 0; i < points * 2; i++) {
            double a = Math.PI * i / points;
            int r = i % 2 == 0 ? outer : inner;
            p[i][0] = cu + (int) Math.round(r * Math.cos(a));
            p[i][1] = cw + (int) Math.round(r * Math.sin(a));
        }
        return p;
    }

    // ---- poses ------------------------------------------------------------

    /**
     * Where the flat (u, w, t) space ends up in the model: an origin and
     * one direction per axis, each as x, y, z. In the game y points down.
     */
    private enum Pose {
        // In the right hand: the tip points forward and up.
        WEAPON_WORN(new double[]{-30, -90, 16}, new double[]{0, -0.40, -0.915}, new double[]{0, -0.915, 0.40},
                new double[]{1, 0, 0}),
        // Lying flat for the inventory icon, along the diagonal.
        WEAPON_INVENTORY(new double[]{-30, 0, -38}, new double[]{0.63, 0, 0.78}, new double[]{-0.78, 0, 0.63},
                new double[]{0, -1, 0}),
        // On the left arm, facing outwards.
        SHIELD_WORN(new double[]{36, -104, 4}, new double[]{0, 0, 1}, new double[]{0, 1, 0},
                new double[]{1, 0, 0}),
        // Lying flat, face up.
        SHIELD_INVENTORY(new double[]{0, 0, -6}, new double[]{1, 0, 0}, new double[]{0, 0, 1},
                new double[]{0, -1, 0}),
        // On top of the head: u and w lie flat, t points up.
        CROWN_WORN(new double[]{0, -190, -6}, new double[]{1, 0, 0}, new double[]{0, 0, 1},
                new double[]{0, -1, 0}),
        CROWN_INVENTORY(new double[]{0, 0, 0}, new double[]{1, 0, 0}, new double[]{0, 0, 1},
                new double[]{0, -1, 0});

        final double[] origin, u, w, t;

        Pose(double[] origin, double[] u, double[] w, double[] t) {
            this.origin = origin;
            this.u = u;
            this.w = w;
            this.t = t;
        }
    }

    // ---- builder ----------------------------------------------------------

    private static final class Builder {

        private final Pose pose;
        /** Mirrors every shape across its length axis. */
        boolean flipWidth;
        private final List<int[]> vertices = new ArrayList<int[]>();
        private final List<int[]> faces = new ArrayList<int[]>();
        private final List<Integer> colours = new ArrayList<Integer>();

        Builder(Pose pose) {
            this.pose = pose;
        }

        private int vertex(double u, double w, double t) {
            if (flipWidth) {
                w = -w;
            }
            int[] v = new int[3];
            for (int k = 0; k < 3; k++) {
                v[k] = (int) Math.round(pose.origin[k] + u * pose.u[k] + w * pose.w[k] + t * pose.t[k]);
            }
            vertices.add(v);
            return vertices.size() - 1;
        }

        /** Adds a face that is visible from both sides. */
        private void face(int a, int b, int c, int colour) {
            faces.add(new int[]{a, b, c});
            colours.add(colour);
            faces.add(new int[]{a, c, b});
            colours.add(colour);
        }

        /**
         * A flat shape (outline in u, w) with the given thickness, centred
         * on t = 0. The outline may be concave as long as every point can
         * be seen from its centre.
         */
        void plate(int[][] outline, int thickness, int colour) {
            int n = outline.length;
            double cu = 0, cw = 0;
            for (int[] p : outline) {
                cu += p[0];
                cw += p[1];
            }
            cu /= n;
            cw /= n;
            double half = thickness / 2.0;
            int topCentre = vertex(cu, cw, half);
            int bottomCentre = vertex(cu, cw, -half);
            int[] top = new int[n], bottom = new int[n];
            for (int i = 0; i < n; i++) {
                top[i] = vertex(outline[i][0], outline[i][1], half);
                bottom[i] = vertex(outline[i][0], outline[i][1], -half);
            }
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                face(topCentre, top[i], top[j], colour);
                face(bottomCentre, bottom[i], bottom[j], colour);
                face(top[i], bottom[i], bottom[j], colour);
                face(top[i], bottom[j], top[j], colour);
            }
        }

        /** A box from u0..u1 and w0..w1 with the given thickness. */
        void box(int u0, int u1, int w0, int w1, int thickness, int colour) {
            plate(new int[][]{{u0, w0}, {u1, w0}, {u1, w1}, {u0, w1}}, thickness, colour);
        }

        /** A spike that sticks out sideways (in t) from the point u, w. */
        void spike(int u, int w, int tBase, int tTip, int halfWidth, int colour) {
            int tip = vertex(u, w, tTip);
            int a = vertex(u - halfWidth, w - halfWidth, tBase);
            int b = vertex(u + halfWidth, w - halfWidth, tBase);
            int c = vertex(u + halfWidth, w + halfWidth, tBase);
            int d = vertex(u - halfWidth, w + halfWidth, tBase);
            face(tip, a, b, colour);
            face(tip, b, c, colour);
            face(tip, c, d, colour);
            face(tip, d, a, colour);
        }

        /**
         * A crown: a round band with points. The band stands on t = 0.
         *
         * @param points number of points; the rim has twice as many corners
         */
        void crown(int radius, int bandHeight, int pointHeight, int points, int colour, int insideColour) {
            int corners = points * 2;
            int[] bottom = new int[corners], top = new int[corners];
            for (int i = 0; i < corners; i++) {
                double a = Math.PI * i / points;
                double u = radius * Math.cos(a), w = radius * Math.sin(a);
                bottom[i] = vertex(u, w, 0);
                // Every other corner rises into a point, leaning slightly outwards.
                boolean point = i % 2 == 0;
                double lean = point ? 1.12 : 1.0;
                top[i] = vertex(u * lean, w * lean, point ? bandHeight + pointHeight : bandHeight);
            }
            for (int i = 0; i < corners; i++) {
                int j = (i + 1) % corners;
                // Outside and inside get their own colour: the two windings of one wall.
                faces.add(new int[]{bottom[i], top[i], top[j]});
                colours.add(colour);
                faces.add(new int[]{bottom[i], top[j], bottom[j]});
                colours.add(colour);
                faces.add(new int[]{bottom[i], top[j], top[i]});
                colours.add(insideColour);
                faces.add(new int[]{bottom[i], bottom[j], top[j]});
                colours.add(insideColour);
            }
        }

        Model toModel(int vertexSkin, int triangleSkin, int priority) {
            int n = vertices.size(), f = faces.size();
            int[] x = new int[n], y = new int[n], z = new int[n];
            for (int i = 0; i < n; i++) {
                x[i] = vertices.get(i)[0];
                y[i] = vertices.get(i)[1];
                z[i] = vertices.get(i)[2];
            }
            int[] a = new int[f], b = new int[f], c = new int[f];
            short[] colour = new short[f];
            for (int i = 0; i < f; i++) {
                a[i] = faces.get(i)[0];
                b[i] = faces.get(i)[1];
                c[i] = faces.get(i)[2];
                colour[i] = (short) (int) colours.get(i);
            }
            return Model.custom(x, y, z, a, b, c, colour, vertexSkin, triangleSkin, priority);
        }
    }
}
