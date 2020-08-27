import matplotlib.pyplot as plt
import seaborn as sns

"""
This is the script we used to generate the plots of the coverage categories.
"""
fig = plt.figure(figsize=(8, 4))
plt.rcParams.update({"font.size": 10})
plt.ylabel("Weighted Percentage of Partial Repairs", labelpad=15, size=12)
plt.ylim(0, 85)
plt.grid(True, "major", "y", alpha=0.4)

ax = plt.gca()
ax.spines["right"].set_visible(False)
ax.spines["left"].set_visible(False)
ax.spines["top"].set_visible(False)
ax.set_axisbelow(True)

def add_to_plot(positive, neutral, negative, offset):
    x_positions_positive = [offset + x for x in [0, 1, 2]]
    x_positions_neutral = [offset + x for x in [5, 6, 7]]
    x_positions_negative = [offset + x for x in [10, 11, 12]]

    width = 0.75
    colors = ["black", "gray", "white"]

    p_positive = plt.bar(
        x_positions_positive,
        positive,
        width,
        edgecolor="black",
        linewidth=.5,
        color=colors,
    )
    p_neutral = plt.bar(
        x_positions_neutral,
        neutral,
        width,
        edgecolor="black",
        linewidth=.5,
        color=colors,
    )
    p_negative = plt.bar(
        x_positions_negative,
        negative,
        width,
        edgecolor="black",
        linewidth=.5,
        color=colors,
    )

    plt.legend(
        (p_positive[0], p_positive[1], p_positive[2]),
        ("Defects4J", "BEARS", "Combined"),
        loc=(0.85, 0.8),
        fontsize=9,
    )

    def label(plot, values):
        for p, val in zip(plot.patches, values):
            width, height = p.get_width(), p.get_height()
            x, y = p.get_xy()
            plt.annotate(f'{val:.2f}', xy=(x + (width / 2), height + 7), ha="center", rotation=90, size=7)

    # Add this loop to add the annotations
    label(p_positive, positive)
    label(p_neutral, neutral)
    label(p_negative, negative)

class_positive = (12.77, 9.22, 12.02)
class_neutral = (68.30, 73.31, 69.35)
class_negative = (11.57, 13.30, 12.16)

add_to_plot(class_positive, class_neutral, class_negative, 0)

method_positive = (34.58, 18.23, 31.14)
method_neutral = (40.05, 64.30, 40.83)
method_negative = (17.16, 13.30, 16.35)

add_to_plot(method_positive, method_neutral, method_negative, 16)

assertion_positive = (39.93, 18.69, 35.46)
assertion_neutral = (31.52, 59.60, 37.43)
assertion_negative = (19.80, 16.41, 19.09)

add_to_plot(method_positive, method_neutral, method_negative, 32)
labels = ["Positive", "Neutral\nClass-Level", "Negative", "Positive", "Neutral\nMethod-Level", "Negative", "Positive", "Neutral\nAssertion-Level", "Negative"]
plt.xticks(ticks=[1, 6, 11, 17, 22, 27, 33, 38, 43], labels=labels)
plt.tick_params(length=0)
plt.axvline(14, color='black')
plt.axvline(30, color='black')
fig.savefig(fname="weighted_percent.pdf", facecolor='w', edgecolor='k', format='pdf')
