package com.skinthesia.data.seed

import com.skinthesia.domain.model.ArticleBlock
import com.skinthesia.domain.model.ArticleCategory
import com.skinthesia.domain.model.LearningArticle
import com.skinthesia.domain.model.SkinGoal

/**
 * Learning library. General, evidence-informed skincare education only;
 * never a diagnosis or a treatment plan.
 */
object ArticleSeed {

    private const val EDITORIAL = "Skinthesia Editorial"
    private const val EDITORIAL_ROLE = "Reviewed by our dermatology panel"

    val articles: List<LearningArticle> = listOf(

        // ---------------------------------------------------------------- Acne

        LearningArticle(
            id = "art-calm-breakout-routine",
            title = "A calm routine for breakout-prone skin",
            subtitle = "Why fewer, gentler steps often work better than more.",
            category = ArticleCategory.ACNE,
            authorName = "Dr. Kavya Menon",
            authorRole = "Consultant Dermatologist",
            expertId = "exp-kavya",
            readMinutes = 4,
            publishedOn = "2026-06-18",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "When breakouts appear, the instinct is often to do more: stronger cleansers, more acids, " +
                        "a new product every week. For most people, the opposite works better. Breakout-prone skin tends to " +
                        "look its best when the routine is short, gentle and repeated for long enough to judge. " +
                        "This guide walks through building that kind of routine, one step at a time.",
                ),
                ArticleBlock.Heading(text = "Start with the basics"),
                ArticleBlock.Paragraph(
                    text = "A calm routine has three steps in the morning and two or three in the evening. Cleanse with a gentle " +
                        "gel or cream cleanser that leaves skin comfortable rather than squeaky. Follow with a lightweight, " +
                        "non-comedogenic moisturizer, even if your skin is oily. In the morning, finish with a broad-spectrum " +
                        "sunscreen. Skin that feels stripped and tight can look more congested, not less, so comfort matters.",
                ),
                ArticleBlock.Heading(text = "Adding one active"),
                ArticleBlock.Paragraph(
                    text = "Once the basics feel easy, choose a single active ingredient and give it room. Salicylic acid helps " +
                        "clear the inside of pores. Niacinamide can help skin look more even and less shiny. Azelaic acid is a " +
                        "gentle option that many people with both breakouts and redness find comfortable. Introduce it two or " +
                        "three evenings a week, then build up only if your skin stays calm.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Keep your routine the same for at least eight weeks before judging it.",
                        "Change one thing at a time, so you know what is helping.",
                        "Leave blemishes alone; squeezing can leave marks that last longer than the spot itself.",
                        "Take progress photos in the same light every two weeks, not every morning.",
                    ),
                ),
                ArticleBlock.Heading(text = "What to expect"),
                ArticleBlock.Paragraph(
                    text = "Skin renews itself slowly, and pores take time to settle. Many people notice small changes in four to " +
                        "six weeks and clearer progress around twelve. A few new spots along the way are normal and do not mean " +
                        "the routine has failed. Tracking your SkinPrint every few weeks gives a steadier picture than a single " +
                        "day in the mirror.",
                ),
                ArticleBlock.Callout(
                    title = "When to see a dermatologist",
                    text = "If breakouts are deep, painful, leaving scars or dark marks, or not settling after three months of " +
                        "consistent care, book a dermatologist. Prescription options exist for exactly these situations, and " +
                        "early advice can save months of trial and error.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "A short, gentle routine is a strong foundation for breakout-prone skin.",
                        "Add one active at a time and give it at least eight weeks.",
                        "Persistent, painful or scarring breakouts deserve a dermatologist's opinion.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.ACNE, SkinGoal.PORES),
            relatedArticleIds = listOf("art-salicylic-acid-explained", "art-skin-barrier-basics"),
        ),

        LearningArticle(
            id = "art-salicylic-acid-explained",
            title = "Salicylic acid, explained simply",
            subtitle = "What this oil-soluble acid does, and how to use it without overdoing it.",
            category = ArticleCategory.ACNE,
            authorName = EDITORIAL,
            authorRole = EDITORIAL_ROLE,
            expertId = null,
            readMinutes = 3,
            publishedOn = "2026-07-09",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "Salicylic acid is one of the most familiar ingredients in breakout care, and one of the most " +
                        "misunderstood. It is not a scrub, and more is not better. Used thoughtfully, a few evenings a week, " +
                        "it can help pores look clearer and skin feel smoother. " +
                        "This guide covers what makes it different from other acids, how to choose a format, and the signs that your skin would like you to slow down.",
                ),
                ArticleBlock.Heading(text = "What makes it different"),
                ArticleBlock.Paragraph(
                    text = "Salicylic acid belongs to a group called beta hydroxy acids, often shortened to BHA. Unlike " +
                        "water-soluble acids such as lactic or glycolic acid, it dissolves in oil. That lets it work inside the " +
                        "pore lining, where oil and dead skin cells can build up into blackheads and congestion. It also has a " +
                        "mild calming quality, which is one reason it is often better tolerated than people expect.",
                ),
                ArticleBlock.Heading(text = "Choosing a format"),
                ArticleBlock.Paragraph(
                    text = "You will find salicylic acid in cleansers, leave-on liquids, serums and spot treatments. Cleansers " +
                        "offer brief contact and suit people who are just starting. Leave-on treatments, commonly between 0.5 " +
                        "and 2 percent, give the ingredient more time on skin. A leave-on liquid used a few evenings a week is " +
                        "a sensible middle ground for most oily and combination skin. " +
                        "Spot treatments can be useful for the occasional blemish, but the whole face rarely needs them. If your skin is dry or sensitive, a cleanser or a lower-strength leave-on is a gentler place to begin.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Apply to clean, dry skin in the evening, then follow with moisturizer.",
                        "Start with two or three evenings a week.",
                        "At first, avoid using it on the same night as a retinoid or another exfoliating acid.",
                        "Wear sunscreen every morning, as freshly exfoliated skin can be more sun-sensitive.",
                    ),
                ),
                ArticleBlock.Heading(text = "Signs to slow down"),
                ArticleBlock.Paragraph(
                    text = "A light tingle on application can be normal. Persistent stinging, tightness, flaking or redness are " +
                        "signs your skin would like a break. Drop back to fewer nights, or pause for a week and focus on " +
                        "cleansing, moisturizing and sunscreen. Irritated skin rarely looks clearer, so patience usually wins. " +
                        "Once things settle, restart at a lower frequency and build up again over a few weeks.",
                ),
                ArticleBlock.Callout(
                    title = "Before you start",
                    text = "If you are pregnant, breastfeeding, using prescription skincare or have very reactive skin, check " +
                        "with a doctor or dermatologist before adding salicylic acid. A patch test on a small area first is " +
                        "always a good idea.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Salicylic acid is oil-soluble, so it can work inside the pore.",
                        "Leave-on formats between 0.5 and 2 percent suit most oily and combination skin.",
                        "Start slowly, moisturize, and wear sunscreen every day.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.ACNE, SkinGoal.PORES, SkinGoal.TEXTURE),
            relatedArticleIds = listOf("art-calm-breakout-routine", "art-layering-actives"),
        ),

        // ---------------------------------------------------------------- Hydration

        LearningArticle(
            id = "art-how-hyaluronic-acid-works",
            title = "How hyaluronic acid holds on to water",
            subtitle = "The science behind the most popular hydrator, and how to get the most from it.",
            category = ArticleCategory.HYDRATION,
            authorName = "Élodie Marchand",
            authorRole = "Clinical Aesthetician",
            expertId = "exp-elodie",
            readMinutes = 4,
            publishedOn = "2026-05-14",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "Hyaluronic acid appears in almost every hydrating serum, and for good reason. It is a sugar-based " +
                        "molecule that skin already makes, and it is remarkably good at attracting and holding water. " +
                        "Understanding how it works helps you use it well, and explains why the same serum can feel wonderful " +
                        "one day and oddly tight the next.",
                ),
                ArticleBlock.Heading(text = "A humectant, not a moisturizer"),
                ArticleBlock.Paragraph(
                    text = "Hyaluronic acid is a humectant. Humectants draw water towards themselves, from the air around you and " +
                        "from deeper layers of skin. In a serum, that means the outer layers look plumper and feel softer. " +
                        "What it does not do on its own is seal that water in. That is the job of a moisturizer, which adds " +
                        "lipids that slow water from escaping. " +
                        "In very dry air, a humectant left without a moisturizer on top can even feel tight, which explains why the same serum can feel different from one day to the next.",
                ),
                ArticleBlock.Heading(text = "Why molecule size matters"),
                ArticleBlock.Paragraph(
                    text = "Many serums combine several weights of hyaluronic acid. Larger molecules sit on the surface and give " +
                        "an immediate smoothing, cushioned feel. Smaller fragments settle into the upper layers of skin, where " +
                        "they help with longer-lasting hydration. A blend of sizes tends to feel comfortable without being " +
                        "sticky, and it works for almost every skin type, including sensitive skin. " +
                        "It also layers easily under other serums, so it rarely needs to compete for a place in your routine.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Apply to damp skin, straight after cleansing or a hydrating mist.",
                        "Use three or four drops; more will not add more hydration.",
                        "Follow with moisturizer within a minute or two.",
                        "In very dry air, choose a slightly richer moisturizer on top.",
                    ),
                ),
                ArticleBlock.Heading(text = "Reading your results"),
                ArticleBlock.Paragraph(
                    text = "If you use the Skinthesia probe, hydration index readings are a helpful way to see how your routine " +
                        "is working over weeks rather than hours. Readings shift with weather, time of day and even a recent " +
                        "shower, so compare them at similar times and look at the trend rather than a single number. " +
                        "Many people find their readings climb gradually over the first few weeks of consistent use, then level off. That plateau is normal and simply means skin has found a comfortable balance.",
                ),
                ArticleBlock.Callout(
                    title = "The Skinthesia view",
                    text = "Hyaluronic acid is a dependable, low-risk first step for most routines. Pair it with a moisturizer " +
                        "that suits your skin type, and you have covered the two halves of hydration: drawing water in and " +
                        "keeping it there.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Hyaluronic acid draws water into the upper layers of skin.",
                        "Apply it to damp skin and follow with moisturizer to hold that water in.",
                        "Judge hydration by the trend over weeks, not a single reading.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.HYDRATION, SkinGoal.TEXTURE, SkinGoal.OVERALL_HEALTH),
            relatedArticleIds = listOf("art-dry-or-dehydrated", "art-skin-barrier-basics"),
        ),

        LearningArticle(
            id = "art-dry-or-dehydrated",
            title = "Dry or dehydrated? Knowing the difference",
            subtitle = "One is about oil, the other about water, and they call for slightly different care.",
            category = ArticleCategory.HYDRATION,
            authorName = EDITORIAL,
            authorRole = EDITORIAL_ROLE,
            expertId = null,
            readMinutes = 3,
            publishedOn = "2026-08-06",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "Dry and dehydrated are often used as if they mean the same thing. They overlap, but they are not " +
                        "identical, and telling them apart makes choosing products much simpler. The short version: dryness " +
                        "is about oil, and dehydration is about water. " +
                        "Many people have a little of both, and the balance can shift with the seasons, so it is worth checking in with your skin every few months.",
                ),
                ArticleBlock.Heading(text = "Dry skin is a skin type"),
                ArticleBlock.Paragraph(
                    text = "Dry skin naturally produces less oil. It tends to feel tight most of the time, can look flaky around " +
                        "the nose and cheeks, and often prefers richer creams. Because there are fewer lipids on the surface, " +
                        "water escapes more easily too, so dry skin frequently feels dehydrated as well. Ceramides, squalane " +
                        "and shea butter help replace some of those missing lipids. " +
                        "Dry skin usually prefers a cream cleanser to a foaming one, and moisturizer applied while skin is still slightly damp.",
                ),
                ArticleBlock.Heading(text = "Dehydrated skin is a temporary state"),
                ArticleBlock.Paragraph(
                    text = "Any skin type, including oily skin, can become dehydrated. It shows up as tightness after cleansing, " +
                        "fine crinkly lines that soften once moisturizer goes on, and a slightly dull look. Air conditioning, " +
                        "seasonal changes, harsh cleansers and over-exfoliating are common contributors. Oily skin that feels " +
                        "tight is often dehydrated rather than dry. " +
                        "The good news is that dehydration usually responds well to small, gentle changes.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Tight all day, flaky and rarely shiny: likely dry.",
                        "Shiny by noon yet tight after washing: likely dehydrated.",
                        "Tight and flaky with some shine in the T-zone: often a bit of both.",
                        "Stinging with most products: take extra care and simplify.",
                    ),
                ),
                ArticleBlock.Heading(text = "Adjusting your routine"),
                ArticleBlock.Paragraph(
                    text = "For dehydration, add water-attracting ingredients such as hyaluronic acid and glycerin, and switch to a " +
                        "gentler cleanser. For dryness, focus on lipid-rich moisturizers and keep hot water to a minimum. If " +
                        "you use the Skinthesia probe, your hydration index can help you see whether changes are making a " +
                        "difference over several weeks. " +
                        "Give any change at least three or four weeks before deciding whether it is working, and change one product at a time so the result is easy to read.",
                ),
                ArticleBlock.Callout(
                    title = "When to see a dermatologist",
                    text = "If dryness is itchy, cracked, painful or spreading, or does not improve with a gentle routine, a " +
                        "dermatologist can help work out what is going on and whether something more than skincare is needed.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Dryness is about oil; dehydration is about water.",
                        "Even oily skin can be dehydrated.",
                        "Humectants help with water, and lipid-rich creams help with dryness.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.HYDRATION, SkinGoal.OVERALL_HEALTH),
            relatedArticleIds = listOf("art-how-hyaluronic-acid-works", "art-skin-barrier-basics"),
        ),

        // ---------------------------------------------------------------- Ingredients

        LearningArticle(
            id = "art-layering-actives",
            title = "How to layer actives without overwhelming skin",
            subtitle = "A simple order of steps, and which ingredients are happier on different nights.",
            category = ArticleCategory.INGREDIENTS,
            authorName = "Dr. Arjun Deshpande",
            authorRole = "Cosmetic Dermatologist",
            expertId = "exp-arjun",
            readMinutes = 4,
            publishedOn = "2026-06-02",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "Actives are ingredients chosen to do a specific job: vitamin C for brightness, retinoids for texture, " +
                        "acids for exfoliation. Each can be helpful on its own. Problems usually start when too many are used " +
                        "at once, too often, or in an order that leaves skin irritated. A little structure goes a long way. " +
                        "The approach below keeps things simple: a dependable order of steps, a sense of which ingredients prefer mornings or evenings, and a gentle pace for adding anything new.",
                ),
                ArticleBlock.Heading(text = "A reliable order"),
                ArticleBlock.Paragraph(
                    text = "The general rule is thinnest to thickest. Cleanse, then apply water-light serums, then thicker serums, " +
                        "then moisturizer. In the morning, sunscreen always comes last. Give each layer a moment to settle " +
                        "before adding the next. If a product has specific directions, such as applying to completely dry skin, " +
                        "follow those first. " +
                        "There is no need to wait long between layers; a minute or so is usually enough for a serum to settle before moisturizer.",
                ),
                ArticleBlock.Heading(text = "Morning and evening roles"),
                ArticleBlock.Paragraph(
                    text = "Some ingredients have natural homes. Vitamin C and other antioxidants suit the morning, where they " +
                        "work alongside sunscreen. Retinoids and exfoliating acids suit the evening, because they can make skin " +
                        "more sun-sensitive and are better left to work overnight. Gentle helpers such as niacinamide and " +
                        "hyaluronic acid are flexible and fit into either routine. " +
                        "If a product does not say when to use it, the evening is usually the safer default for anything exfoliating.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Niacinamide and vitamin C can be used in the same routine by most people.",
                        "Retinoids and exfoliating acids are usually better on alternate nights.",
                        "Only one new active at a time, with two to four weeks before adding another.",
                        "If skin stings, go back to basics for a week, then reintroduce slowly.",
                    ),
                ),
                ArticleBlock.Heading(text = "Less is often more"),
                ArticleBlock.Paragraph(
                    text = "A routine with one or two actives, used consistently, often does more than five used occasionally. " +
                        "Skin needs time to adjust, and irritation can undo progress. I often suggest a simple rotation: " +
                        "a retinoid night, an exfoliating night, then a recovery night with just moisturizer. " +
                        "Over time, as your skin grows used to each active, you can adjust the rhythm, but the recovery night is often worth keeping.",
                ),
                ArticleBlock.Callout(
                    title = "The Skinthesia view",
                    text = "Your plan in Skinthesia spaces actives across mornings and evenings for you. If you add a product of " +
                        "your own, patch test it first and check it against your plan so nothing doubles up.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Layer from thinnest to thickest, with sunscreen last in the morning.",
                        "Keep vitamin C for mornings and retinoids or acids for evenings.",
                        "Introduce one active at a time and give it weeks, not days.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.TEXTURE, SkinGoal.FINE_LINES, SkinGoal.OVERALL_HEALTH),
            relatedArticleIds = listOf("art-patch-testing", "art-tone-ingredients-compared"),
        ),

        LearningArticle(
            id = "art-patch-testing",
            title = "Patch testing, step by step",
            subtitle = "A few quiet days of testing can spare your whole face a bad week.",
            category = ArticleCategory.INGREDIENTS,
            authorName = EDITORIAL,
            authorRole = EDITORIAL_ROLE,
            expertId = null,
            readMinutes = 3,
            publishedOn = "2026-05-02",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "Patch testing is the least exciting part of skincare, and one of the most useful. It means trying a " +
                        "new product on a small area before using it all over your face. It will not predict every reaction, " +
                        "but it catches many of them early, when they are small and easy to manage. " +
                        "It is especially worth doing if your skin is sensitive, if you have reacted to products before, or if you are trying a new active ingredient.",
                ),
                ArticleBlock.Heading(text = "Where and how"),
                ArticleBlock.Paragraph(
                    text = "Choose an area that is easy to watch and similar to facial skin, such as the side of the neck, just " +
                        "below the jawline, or behind the ear. Apply a small amount of the product as you normally would, once " +
                        "or twice a day. For rinse-off products, apply and rinse as directed. Keep the rest of your routine the " +
                        "same, so you know what you are testing. " +
                        "Rinse-off products carry a lower risk, so a few days is often enough for a cleanser, while leave-on products deserve the full week.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Test one new product at a time.",
                        "Continue for five to seven days, since some reactions take time to appear.",
                        "Watch for redness, itching, stinging, bumps or swelling.",
                        "Note the product and dates in your Skinthesia journal.",
                    ),
                ),
                ArticleBlock.Heading(text = "Reading the result"),
                ArticleBlock.Paragraph(
                    text = "If the area looks and feels the same after a week, move to a small section of the face, such as one " +
                        "cheek, for a few more days, then use it as planned. A brief, mild tingle with acids or vitamin C can be " +
                        "normal. Anything that itches, burns, swells or lingers is a reason to stop and rinse the area gently. " +
                        "A clear patch test lowers the risk of a reaction but cannot rule it out completely, so keep an eye on your skin during the first few weeks of regular use.",
                ),
                ArticleBlock.Heading(text = "Why it matters more for actives"),
                ArticleBlock.Paragraph(
                    text = "Retinoids, exfoliating acids and higher-strength vitamin C are more likely to cause irritation than " +
                        "cleansers or plain moisturizers. Testing them slowly, and introducing them gradually afterwards, " +
                        "gives your skin the best chance of adjusting comfortably.",
                ),
                ArticleBlock.Callout(
                    title = "When to see a dermatologist",
                    text = "If a reaction is severe, spreads beyond the test area, blisters, or involves swelling around the eyes " +
                        "or lips, stop using the product and seek medical advice promptly.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Patch test each new product for five to seven days.",
                        "Test one product at a time on a small, easy-to-watch area.",
                        "Stop and seek advice if a reaction is strong or spreading.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.REDNESS, SkinGoal.OVERALL_HEALTH),
            relatedArticleIds = listOf("art-layering-actives", "art-ph-and-your-skin"),
        ),

        // ---------------------------------------------------------------- Pigmentation

        LearningArticle(
            id = "art-why-dark-spots-appear",
            title = "Why do dark spots appear?",
            subtitle = "How pigment forms, why marks linger, and what helps them look softer over time.",
            category = ArticleCategory.PIGMENTATION,
            authorName = "Dr. Ishita Banerjee",
            authorRole = "Pigmentation Specialist",
            expertId = "exp-ishita",
            readMinutes = 4,
            publishedOn = "2026-08-20",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "Dark spots are among the most common reasons people start a skincare routine. They are also among " +
                        "the slowest to change, which can be frustrating. Knowing where they come from makes the waiting easier, " +
                        "and it explains why sunscreen sits at the centre of almost every plan I write.",
                ),
                ArticleBlock.Heading(text = "Where pigment comes from"),
                ArticleBlock.Paragraph(
                    text = "Skin colour comes from melanin, made by cells called melanocytes. Melanin is protective: when skin " +
                        "senses ultraviolet light, heat or inflammation, melanocytes can make more of it. Sometimes that response " +
                        "is uneven, and pigment gathers in one small area. The result is a spot that looks darker than the skin " +
                        "around it. " +
                        "In medium and deeper skin tones, melanocytes tend to respond more strongly, which is one reason marks can be more noticeable and slower to fade.",
                ),
                ArticleBlock.Heading(text = "Common kinds of dark spots"),
                ArticleBlock.Paragraph(
                    text = "Marks left after a blemish, bite or scratch are very common, especially in medium and deeper skin " +
                        "tones, and often look brown or grey-brown. Sun spots tend to appear on areas that see the most daylight, " +
                        "such as the cheeks, forehead and the backs of the hands. Broader patches of pigment can be influenced by " +
                        "sun, heat and hormones, and they are worth discussing with a dermatologist. " +
                        "Knowing which kind you are looking at helps set realistic expectations, since each tends to change at its own pace.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Daily broad-spectrum sunscreen, reapplied when you are outdoors.",
                        "A morning antioxidant such as vitamin C.",
                        "A tone-evening ingredient such as niacinamide, tranexamic acid or azelaic acid.",
                        "Leaving blemishes alone, so fewer new marks form.",
                    ),
                ),
                ArticleBlock.Heading(text = "Why patience matters"),
                ArticleBlock.Paragraph(
                    text = "Pigment sits in skin that renews itself gradually, so visible change is measured in months rather than " +
                        "days. Unprotected sun exposure can quickly undo progress, which is why sunscreen matters more than any " +
                        "single serum. Compare photos taken in the same light every four to six weeks, and you are more likely " +
                        "to notice the slow, steady softening. " +
                        "It also helps to choose one tone-evening ingredient and give it a fair trial of at least twelve weeks, because constant changes make it hard to see what is working.",
                ),
                ArticleBlock.Callout(
                    title = "When to see a dermatologist",
                    text = "Any spot that is new and changing, has an irregular border or several colours, itches or bleeds should " +
                        "be checked by a dermatologist promptly. The same applies to large patches that appear suddenly.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Dark spots form when pigment gathers unevenly in one area.",
                        "Sunscreen protects progress more than any single serum.",
                        "Expect gradual change over months, and have changing spots checked.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE),
            relatedArticleIds = listOf("art-tone-ingredients-compared", "art-reading-a-sunscreen-label"),
            featured = true,
        ),

        LearningArticle(
            id = "art-tone-ingredients-compared",
            title = "Vitamin C, niacinamide or tranexamic acid?",
            subtitle = "Three popular tone-evening ingredients, side by side.",
            category = ArticleCategory.PIGMENTATION,
            authorName = EDITORIAL,
            authorRole = EDITORIAL_ROLE,
            expertId = null,
            readMinutes = 4,
            publishedOn = "2026-07-23",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "If you are hoping for a more even tone, you will quickly meet the same three names on almost every " +
                        "label. Each works a little differently, suits slightly different skin, and fits a different moment in " +
                        "your routine. You rarely need all three. Here is how they compare. " +
                        "All three are generally well tolerated and widely used in cosmetic skincare, and all three work best alongside daily sunscreen.",
                ),
                ArticleBlock.Heading(text = "Vitamin C"),
                ArticleBlock.Paragraph(
                    text = "Vitamin C is an antioxidant that helps skin look brighter and supports your sunscreen against daily " +
                        "environmental stress. Pure ascorbic acid is effective but can sting and is easily degraded by light " +
                        "and air. Gentler forms, such as ethyl ascorbic acid and ascorbyl glucoside, are more stable and often " +
                        "better tolerated. It belongs in the morning, under sunscreen. " +
                        "Store it away from sunlight with the cap closed tightly; a serum that turns dark orange has likely lost some of its strength.",
                ),
                ArticleBlock.Heading(text = "Niacinamide"),
                ArticleBlock.Paragraph(
                    text = "Niacinamide, a form of vitamin B3, is the easy-going option. At around 2 to 5 percent it helps tone " +
                        "look more even, supports the barrier and can help pores look refined. Most skin types tolerate it well, " +
                        "and it fits into morning or evening routines without much planning. " +
                        "Very high strengths are not necessarily better, and some people find them more likely to cause flushing, so a moderate level is a sensible start.",
                ),
                ArticleBlock.Heading(text = "Tranexamic acid"),
                ArticleBlock.Paragraph(
                    text = "Tranexamic acid is a newer favourite in topical skincare. It is typically used at 2 to 5 percent and is " +
                        "known for being gentle, which makes it a good choice for sensitive skin. Many people find it helps " +
                        "stubborn marks and uneven patches look softer, especially alongside consistent sun protection. " +
                        "It pairs comfortably with niacinamide, and the two often appear together in the same serum. As with any new active, patch test it first if your skin is reactive.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Want brightness and extra support for your sunscreen? Consider vitamin C.",
                        "Want an all-rounder that also helps pores? Consider niacinamide.",
                        "Sensitive skin with lingering marks? Consider tranexamic acid.",
                        "Whichever you choose, use it consistently for at least twelve weeks before judging the results.",
                    ),
                ),
                ArticleBlock.Callout(
                    title = "The Skinthesia view",
                    text = "One well-chosen ingredient, used every day with sunscreen, usually does more than three rotated at random. " +
                        "Your plan picks one based on your SkinPrint and preferences, and you can swap it if it does not suit you.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Vitamin C suits the morning and pairs naturally with sunscreen.",
                        "Niacinamide is a flexible, well-tolerated all-rounder.",
                        "Tranexamic acid is a gentle option for lingering marks.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.UNEVEN_TONE, SkinGoal.DARK_SPOTS),
            relatedArticleIds = listOf("art-why-dark-spots-appear", "art-layering-actives"),
        ),

        // ---------------------------------------------------------------- Sunscreen

        LearningArticle(
            id = "art-reading-a-sunscreen-label",
            title = "Reading a sunscreen label",
            subtitle = "SPF, PA ratings and broad spectrum, explained in plain words.",
            category = ArticleCategory.SUNSCREEN,
            authorName = "Dr. Arjun Deshpande",
            authorRole = "Cosmetic Dermatologist",
            expertId = "exp-arjun",
            readMinutes = 4,
            publishedOn = "2026-05-21",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "Sunscreen labels are crowded with numbers and symbols, and it is easy to choose on texture alone. " +
                        "Texture matters, because you will only wear a sunscreen you like. But a few minutes spent understanding " +
                        "the label helps you pick one that protects the way you expect. " +
                        "Here are the three things I look at first, and how to read them.",
                ),
                ArticleBlock.Heading(text = "SPF is about UVB"),
                ArticleBlock.Paragraph(
                    text = "SPF, or sun protection factor, describes protection against UVB, the rays mainly responsible for " +
                        "sunburn. Higher numbers filter more UVB, but the difference between SPF 30 and SPF 50 is smaller than it " +
                        "looks. What matters more is applying enough. Most people use about half the amount tested in the lab, " +
                        "which lowers the protection they actually get. " +
                        "A higher SPF gives you a little margin for real-world application, but it is never a reason to stay out longer.",
                ),
                ArticleBlock.Heading(text = "PA and broad spectrum are about UVA"),
                ArticleBlock.Paragraph(
                    text = "UVA reaches deeper into skin, passes through clouds and window glass, and plays a large part in dark " +
                        "spots, uneven tone and visible ageing. The PA rating, shown with plus signs, indicates UVA protection, " +
                        "with PA++++ the highest. A broad-spectrum label means the product protects against both UVA and UVB. " +
                        "Where daylight is strong for much of the year, UVA protection deserves as much attention as the SPF number, especially if you are working on dark spots.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Look for SPF 30 or higher, with SPF 50 for long days outdoors.",
                        "Choose PA+++ or PA++++, or a clear broad-spectrum claim.",
                        "Use about two finger-lengths for the face and neck.",
                        "Reapply every two hours outdoors, and after sweating or swimming.",
                    ),
                ),
                ArticleBlock.Heading(text = "Mineral or modern filters?"),
                ArticleBlock.Paragraph(
                    text = "Mineral sunscreens use zinc oxide or titanium dioxide, and many sensitive skins find them comfortable, " +
                        "though some can leave a cast on deeper tones. Modern organic filters tend to feel lighter and disappear " +
                        "more easily. Both can protect well. The best choice is the one you will apply generously every morning. " +
                        "Tinted mineral formulas are a good middle ground for many people, because the tint helps reduce any cast.",
                ),
                ArticleBlock.Callout(
                    title = "The Skinthesia view",
                    text = "Sunscreen is the step that protects every other step. If your routine only has room for three products, " +
                        "make one of them a broad-spectrum SPF you enjoy wearing.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "SPF describes UVB protection; PA and broad spectrum describe UVA.",
                        "Applying enough matters as much as the number on the tube.",
                        "Choose the sunscreen you will actually wear every day.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE, SkinGoal.OVERALL_HEALTH),
            relatedArticleIds = listOf("art-reapplying-over-makeup", "art-why-dark-spots-appear"),
        ),

        LearningArticle(
            id = "art-reapplying-over-makeup",
            title = "Reapplying sunscreen over makeup",
            subtitle = "Practical ways to top up protection without starting your face again.",
            category = ArticleCategory.SUNSCREEN,
            authorName = "Dr. Ishita Banerjee",
            authorRole = "Pigmentation Specialist",
            expertId = "exp-ishita",
            readMinutes = 3,
            publishedOn = "2026-09-03",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "Most of us apply sunscreen in the morning and forget about it. Protection fades through the day as " +
                        "sunscreen is rubbed, sweated or blotted away, which matters most on days spent outdoors. For anyone " +
                        "working on dark spots or uneven tone, reapplying is one of the most useful habits to build, " +
                        "even when you are wearing makeup.",
                ),
                ArticleBlock.Heading(text = "When reapplying matters"),
                ArticleBlock.Paragraph(
                    text = "If you spend most of the day indoors, away from windows, your morning layer does much of the work. " +
                        "Reapplying becomes important when you are outside for longer stretches, sitting by a sunny window, " +
                        "commuting in daylight, or after sweating. As a guide, top up every two hours when you are outdoors. " +
                        "Reapply sooner after swimming, heavy sweating or towelling your face.",
                ),
                ArticleBlock.Heading(text = "Methods that respect makeup"),
                ArticleBlock.Paragraph(
                    text = "The aim is to add a fresh, even layer without dragging what is underneath. Start by blotting shine " +
                        "with a tissue. Then choose the method that suits your day. Pressing is kinder than rubbing, and thin " +
                        "layers build up more evenly than one thick one. " +
                        "If you wear foundation, a fluid sunscreen pressed on with a damp sponge tends to blend most invisibly, while a stick is quick and tidy on bare or lightly made-up skin. Mists and powders are convenient, but it is hard to apply enough of them to rely on alone.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Fluid sunscreen pressed on with a damp sponge, in two thin layers.",
                        "A sunscreen stick glided over the high points of the face, then patted in.",
                        "A mist held at arm's length, as a top-up on the move.",
                        "A mineral powder with SPF, best treated as an extra rather than your main protection.",
                    ),
                ),
                ArticleBlock.Heading(text = "Making it a habit"),
                ArticleBlock.Paragraph(
                    text = "Keep a small sunscreen and a clean sponge in your bag or at your desk. Link reapplying to something you " +
                        "already do, such as lunch or leaving the office. A hat and shade on bright afternoons help too, " +
                        "and they never need topping up. " +
                        "On days you will be outside for hours, it is often easier to skip makeup altogether and reapply sunscreen freely.",
                ),
                ArticleBlock.Callout(
                    title = "The Skinthesia view",
                    text = "Perfect reapplication is not the goal. Any reasonable top-up on a sunny day is better than none, " +
                        "and your routine reminders can prompt you at lunchtime if you would like a nudge.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Top up every two hours when you are outdoors.",
                        "Blot first, then press on thin layers rather than rubbing.",
                        "Powders and mists are handy extras, not a replacement for your morning layer.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE),
            relatedArticleIds = listOf("art-reading-a-sunscreen-label", "art-why-dark-spots-appear"),
        ),

        // ---------------------------------------------------------------- Skin health

        LearningArticle(
            id = "art-skin-barrier-basics",
            title = "Your skin barrier, in plain words",
            subtitle = "What the barrier is, how to tell it needs care, and how to support it.",
            category = ArticleCategory.SKIN_HEALTH,
            authorName = "Dr. Kavya Menon",
            authorRole = "Consultant Dermatologist",
            expertId = "exp-kavya",
            readMinutes = 4,
            publishedOn = "2026-09-10",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "The skin barrier comes up in almost every conversation about skincare, often without much explanation. " +
                        "It is simply the outermost layer of your skin, and it has two main jobs: keeping water in and keeping " +
                        "irritants out. When it is working well, skin tends to feel comfortable and look calm. " +
                        "When it is under strain, even gentle products can feel uncomfortable, which is why so much skincare advice starts here.",
                ),
                ArticleBlock.Heading(text = "Bricks and mortar"),
                ArticleBlock.Paragraph(
                    text = "A helpful picture is a brick wall. The bricks are flattened skin cells, and the mortar between them is " +
                        "a mix of lipids, mainly ceramides, cholesterol and fatty acids. Together they form a flexible seal. " +
                        "Natural moisturizing factors inside the cells help hold water, and a thin, slightly acidic film on the " +
                        "surface helps keep everything in balance. " +
                        "Skincare cannot rebuild the wall for you, but ingredients that resemble the mortar, such as ceramides, cholesterol and squalane, can help skin feel more comfortable while it does its own work.",
                ),
                ArticleBlock.Heading(text = "Signs it may need care"),
                ArticleBlock.Paragraph(
                    text = "When the barrier is under strain, water escapes more easily and products that used to feel fine may " +
                        "start to sting. Over-cleansing, frequent exfoliation, too many actives at once, very hot water and dry " +
                        "air are common contributors. The good news is that a barrier under strain often feels more comfortable " +
                        "within a few weeks of gentler care. " +
                        "A short note of what changed in your routine before the discomfort started can help you spot what to pause.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Tightness soon after cleansing.",
                        "Stinging with products that never used to sting.",
                        "Flaking, rough patches or a shiny-yet-tight feeling.",
                        "Redness that appears more easily than usual.",
                    ),
                ),
                ArticleBlock.Heading(text = "A simple reset"),
                ArticleBlock.Paragraph(
                    text = "For two to four weeks, pare your routine back to a gentle cleanser, a moisturizer with ceramides or " +
                        "squalane, and sunscreen in the morning. Pause exfoliating acids and retinoids. Use lukewarm water and pat " +
                        "skin dry. Once skin feels comfortable again, reintroduce actives one at a time, starting slowly. " +
                        "If you use the probe, hydration readings can be a helpful way to follow that comfort returning over the weeks.",
                ),
                ArticleBlock.Callout(
                    title = "When to see a dermatologist",
                    text = "If discomfort, redness or flaking continues after a few weeks of gentle care, or if skin is cracked, " +
                        "weeping or very itchy, a dermatologist can help find out what is going on.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "The barrier keeps water in and irritants out.",
                        "Stinging and tightness are common signs it needs a gentler routine.",
                        "Pare back, protect and moisturize, then reintroduce actives slowly.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.OVERALL_HEALTH, SkinGoal.HYDRATION, SkinGoal.REDNESS),
            relatedArticleIds = listOf("art-ph-and-your-skin", "art-dry-or-dehydrated"),
            featured = true,
        ),

        LearningArticle(
            id = "art-ph-and-your-skin",
            title = "What pH really means for your skin",
            subtitle = "Separating the useful science from the myths.",
            category = ArticleCategory.SKIN_HEALTH,
            authorName = EDITORIAL,
            authorRole = EDITORIAL_ROLE,
            expertId = null,
            readMinutes = 3,
            publishedOn = "2026-06-25",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "pH is one of those words that sounds technical and gets used loosely. Some posts suggest that one wrong " +
                        "product will throw your skin out of balance for days. The reality is calmer and more interesting. " +
                        "Skin is naturally a little acidic, and it is quite good at looking after that on its own. " +
                        "Here is what pH means, where it matters, and which popular claims you can safely set aside.",
                ),
                ArticleBlock.Heading(text = "The basics"),
                ArticleBlock.Paragraph(
                    text = "pH measures how acidic or alkaline something is, on a scale from 0 to 14, with 7 as neutral. The skin " +
                        "surface usually sits somewhere between about 4.5 and 5.5. This mild acidity, sometimes called the acid " +
                        "mantle, supports the enzymes that maintain the barrier and helps keep the skin's natural microbes in " +
                        "balance. " +
                        "Cleansing, water and products can nudge the surface pH up or down for a short while, and healthy skin gently brings it back.",
                ),
                ArticleBlock.Heading(text = "Myths worth letting go"),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Myth: you need a cleanser with exactly pH 5.5. Many gentle cleansers sit in a comfortable range, and how skin feels matters more.",
                        "Myth: products must be applied in pH order. Most modern formulas work well in a simple thin-to-thick routine.",
                        "Myth: a single high-pH product ruins your barrier. Healthy skin usually returns to its normal range within hours.",
                    ),
                ),
                ArticleBlock.Paragraph(
                    text = "These myths persist because pH is easy to measure and sounds precise. In practice, the overall " +
                        "gentleness of a formula, including its cleansing agents, fragrance and how often you use it, has a " +
                        "bigger effect on comfort than a small difference in pH.",
                ),
                ArticleBlock.Heading(text = "Where pH does matter"),
                ArticleBlock.Paragraph(
                    text = "Traditional bar soaps are often quite alkaline, and frequent use can leave some people feeling tight and " +
                        "dry. Exfoliating acids are formulated at a lower pH so they can work, which is also why they can tingle. " +
                        "If you use the Skinthesia probe, pH readings are one piece of context, best read as a trend alongside " +
                        "how your skin feels. " +
                        "Readings can shift after cleansing, exercise or applying products, so taking them at a similar time each day makes them easier to compare.",
                ),
                ArticleBlock.Callout(
                    title = "The Skinthesia view",
                    text = "Choose gentle products that leave your skin comfortable, avoid over-cleansing, and let skin do much of " +
                        "the balancing itself. There is no need to test every product with a pH strip.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Skin is naturally slightly acidic, usually around 4.5 to 5.5.",
                        "Healthy skin rebalances quickly after cleansing.",
                        "Comfort after washing is a better guide than a number on a label.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.OVERALL_HEALTH),
            relatedArticleIds = listOf("art-skin-barrier-basics", "art-patch-testing"),
        ),

        // ---------------------------------------------------------------- Lifestyle

        LearningArticle(
            id = "art-sleep-and-steady-routines",
            title = "Rest, rhythm and a steady routine",
            subtitle = "How regular habits support general wellbeing, and what we can and cannot say about skin.",
            category = ArticleCategory.LIFESTYLE,
            authorName = EDITORIAL,
            authorRole = EDITORIAL_ROLE,
            expertId = null,
            readMinutes = 3,
            publishedOn = "2026-08-13",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "It is tempting to look for one habit that explains how our skin looks. Real life is more complicated. " +
                        "Skin is shaped by genetics, climate, hormones, age, products and many things we cannot see. Sleep and " +
                        "daily rhythm are part of that picture, as part of general wellbeing, rather than a switch that turns " +
                        "results on or off. " +
                        "This piece looks at what the evidence can and cannot tell us.",
                ),
                ArticleBlock.Heading(text = "What we can say"),
                ArticleBlock.Paragraph(
                    text = "Skin, like the rest of the body, follows daily rhythms. Research suggests that some repair processes and " +
                        "water loss vary across the day and night. Many people also notice that after a run of short nights their " +
                        "skin looks a little less bright, or the under-eye area looks more shadowed. These observations are " +
                        "common, but they vary widely from person to person. " +
                        "We share them as everyday experience rather than firm science.",
                ),
                ArticleBlock.Heading(text = "What we cannot say"),
                ArticleBlock.Paragraph(
                    text = "There is no reliable evidence that a set number of hours will change a particular concern, and a late " +
                        "night is not the cause of a breakout or a dark spot. Skinthesia uses lifestyle as context. We show it " +
                        "alongside your SkinPrint so you can notice patterns, never as a verdict on what you did wrong. " +
                        "If you notice a pattern in your journal, see it as a useful observation rather than a rule.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Keep your evening routine at roughly the same time each night.",
                        "Place your products where you get ready for bed, so the routine is easy to start.",
                        "On tired nights, a two-step routine of cleanser and moisturizer is enough.",
                        "Use your journal to note how you feel, without judging it.",
                    ),
                ),
                ArticleBlock.Heading(text = "Consistency over perfection"),
                ArticleBlock.Paragraph(
                    text = "Routines tend to work best when they are repeated, not when they are flawless. A simple evening rhythm " +
                        "makes that repetition easier, and a regular wind-down is pleasant in its own right. " +
                        "If your evenings are unpredictable, link your routine to something that happens every night anyway, such as brushing your teeth. Small anchors like this make habits easier to keep than willpower alone.",
                ),
                ArticleBlock.Callout(
                    title = "When to talk to a professional",
                    text = "If ongoing sleep difficulties are affecting how you feel day to day, speak to a doctor. If your skin " +
                        "changes suddenly or feels painful, see a dermatologist.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Lifestyle is context for your skin, not a cause to blame.",
                        "A steady evening rhythm makes a routine easier to keep.",
                        "On tired nights, a shorter routine still counts.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.OVERALL_HEALTH, SkinGoal.DARK_CIRCLES),
            relatedArticleIds = listOf("art-routine-while-travelling", "art-skin-barrier-basics"),
        ),

        LearningArticle(
            id = "art-routine-while-travelling",
            title = "Keeping your routine while you travel",
            subtitle = "A pared-back kit and a few habits that make routines portable.",
            category = ArticleCategory.LIFESTYLE,
            authorName = "Élodie Marchand",
            authorRole = "Clinical Aesthetician",
            expertId = "exp-elodie",
            readMinutes = 3,
            publishedOn = "2026-07-30",
            blocks = listOf(
                ArticleBlock.Paragraph(
                    text = "Travel changes almost everything around your skin at once: the air, the water, the climate, your " +
                        "sleep and your schedule. It is completely normal for skin to feel a little different on a trip. " +
                        "The aim is not to keep every step, but to keep the few that matter most. " +
                        "With a little planning, a routine can travel almost as easily as a toothbrush.",
                ),
                ArticleBlock.Heading(text = "Pack the essentials"),
                ArticleBlock.Paragraph(
                    text = "For most trips, four products are enough: a gentle cleanser, a moisturizer, sunscreen and, if you like, " +
                        "one serum you already use. Decant into small, labelled containers. For short trips, I usually suggest " +
                        "leaving strong actives such as retinoids or acids at home, so you are not managing irritation somewhere new. " +
                        "For longer stays, bring your usual active and return to your normal rhythm once you have settled in. A small zip pouch keeps bottles upright and makes security checks quicker.",
                ),
                ArticleBlock.Bullets(
                    items = listOf(
                        "Gentle cleanser, decanted into a small bottle.",
                        "A moisturizer a little richer than usual for flights and hotel air.",
                        "Sunscreen in your day bag, plus a small one for reapplying.",
                        "Lip balm and a travel pouch that keeps everything together.",
                    ),
                ),
                ArticleBlock.Heading(text = "On the plane"),
                ArticleBlock.Paragraph(
                    text = "Cabin air is very dry. On longer flights, a hydrating serum and moisturizer applied after boarding can " +
                        "help skin feel more comfortable. If you are flying during the day, remember that sunlight through the " +
                        "window still counts, so a layer of sunscreen is worthwhile. " +
                        "Skip heavy makeup on long flights if you can, and sip water regularly so you feel more comfortable overall.",
                ),
                ArticleBlock.Heading(text = "Adapting to a new climate"),
                ArticleBlock.Paragraph(
                    text = "In humid places, a gel moisturizer may feel better than a cream. In cold or dry places, you may want a " +
                        "richer layer at night. Keep changes small and temporary, then return to your usual routine at home. " +
                        "Your plan will still be there when you get back. " +
                        "If you try a new product while you are away, patch test it first, and avoid starting anything strong right before an important event or a beach holiday.",
                ),
                ArticleBlock.Callout(
                    title = "The Skinthesia view",
                    text = "Missing a few steps while travelling will not undo your progress. Consistency is measured over weeks, " +
                        "and returning to your routine is what counts. " +
                        "If your skin reacts strongly while you are away, a pharmacist or local doctor can help.",
                ),
                ArticleBlock.Takeaways(
                    items = listOf(
                        "Four familiar products cover most trips.",
                        "Leave strong actives at home for short stays.",
                        "Small, temporary adjustments suit a new climate best.",
                    ),
                ),
            ),
            relevantGoals = setOf(SkinGoal.OVERALL_HEALTH, SkinGoal.HYDRATION),
            relatedArticleIds = listOf("art-sleep-and-steady-routines", "art-reapplying-over-makeup"),
        ),
    )
}
