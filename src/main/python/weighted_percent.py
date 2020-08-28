import matplotlib.pyplot as plt
import seaborn as sns

"""
This is the script we used to generate the plots of the coverage categories.
"""
fig = plt.figure(figsize=(10, 4))
plt.rcParams.update({"font.size": 14})
plt.ylabel("Weighted % of Partial Repairs", labelpad=10, size=16)
plt.ylim(0, 85)
plt.xlim(-1, 39)
plt.grid(True, "major", "y", alpha=0.4)
plt.margins(tight=True, x=0, y=0)

ax = plt.gca()
ax.spines["right"].set_visible(False)
ax.spines["left"].set_visible(False)
ax.spines["top"].set_visible(False)
ax.set_axisbelow(True)
ax.yaxis.get_major_ticks()[0].label1.set_visible(False)

def add_to_plot(positive, neutral, negative, offset):
    x_positions_positive = [offset + x for x in [0, 1, 2]]
    x_positions_neutral = [offset + x for x in [4, 5, 6]]
    x_positions_negative = [offset + x for x in [8, 9, 10]]

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
        loc=(.17, 1.05),
        ncol=3,
    )

    def label(plot, values):
        for p, val in zip(plot.patches, values):
            width, height = p.get_width(), p.get_height()
            x, y = p.get_xy()
            plt.annotate(f'{val:.1f}', xy=(x + (width / 2), height + 9), ha="center", rotation=90, size=12)

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

add_to_plot(method_positive, method_neutral, method_negative, 14)

assertion_positive = (39.93, 18.69, 35.46)
assertion_neutral = (31.52, 59.60, 37.43)
assertion_negative = (19.80, 16.41, 19.09)

add_to_plot(method_positive, method_neutral, method_negative, 28)
labels = ["Positive", "Neutral\nClass-Level", "Negative", "Positive", "Neutral\nMethod-Level", "Negative", "Positive", "Neutral\nAssertion-Level", "Negative"]
plt.xticks(ticks=[1, 5, 9, 15, 19, 23, 29, 33, 37], labels=labels, size=12.5)
plt.tick_params(length=0)
plt.axvline(12, color='black')
plt.axvline(26, color='black')
fig.savefig(fname="weighted_percent.pdf", facecolor='w', edgecolor='k', format='pdf', pad_inches=0, bbox_inches="tight")
