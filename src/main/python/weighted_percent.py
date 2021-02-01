import matplotlib.pyplot as plt
import seaborn as sns

"""
This is the script we used to generate the plots of the coverage categories.
"""
fig = plt.figure(figsize=(8, 1.25))
plt.rcParams.update({"font.size": 8})
plt.ylabel("Weighted %\nof Partial Repairs", labelpad=10, size=7)
plt.ylim(0, 89)
plt.xlim(-1, 39)
ticks = (20, 40, 60, 80)
plt.grid(True, "major", "y", alpha=0.4, markevery=20)
plt.margins(tight=True, x=0, y=0)

ax = plt.gca()
ax.spines["right"].set_visible(False)
ax.spines["left"].set_visible(False)
ax.spines["top"].set_visible(False)
ax.set_axisbelow(True)
ax.set_yticks(ticks)

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
        loc=(.9, .6),
        fontsize=6,
    )

    def label(plot, values):
        for p, val in zip(plot.patches, values):
            width, height = p.get_width(), p.get_height()
            x, y = p.get_xy()
            plt.annotate(f'{val:.1f}', xy=(x + (width / 2), height + 3), ha="center", rotation=90, size=7)

    # Add this loop to add the annotations
    label(p_positive, positive)
    label(p_neutral, neutral)
    label(p_negative, negative)

class_positive = (12.5, 8.8, 11.7)
class_neutral = (67.4, 70.5, 68.1)
class_negative = (12.8, 13.8, 13.0)

add_to_plot(class_positive, class_neutral, class_negative, 0)

method_positive = (34.0, 17.4, 30.4)
method_neutral = (39.6, 61.9, 44.4)
method_negative = (18.0, 13.8, 17.1)

add_to_plot(method_positive, method_neutral, method_negative, 14)

assertion_positive = (39.2, 17.9, 34.6)
assertion_neutral = (31.3, 57.4, 36.9)
assertion_negative = (20.6, 16.8, 19.8)

add_to_plot(assertion_positive, assertion_neutral, assertion_negative, 28)
labels = ["Positive", "Neutral\nClass-Level", "Negative", "Positive", "Neutral\nMethod-Level", "Negative", "Positive", "Neutral\nAssertion-Level", "Negative"]
plt.xticks(ticks=[1, 5, 9, 15, 19, 23, 29, 33, 37], labels=labels, size=7)
plt.tick_params(length=0)
plt.axvline(12, color='black')
plt.axvline(26, color='black')
fig.savefig(fname="weighted_percent.pdf", facecolor='w', edgecolor='k', format='pdf', pad_inches=0, bbox_inches="tight")
