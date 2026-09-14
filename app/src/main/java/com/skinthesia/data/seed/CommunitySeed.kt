package com.skinthesia.data.seed

import com.skinthesia.domain.model.CommunityAuthor
import com.skinthesia.domain.model.CommunityComment
import com.skinthesia.domain.model.CommunityPost
import com.skinthesia.domain.model.CommunitySection
import com.skinthesia.domain.model.SkinGoal

/**
 * Seed conversations for the community. Text-first and supportive; members share
 * what worked for them, and experts keep answers general and responsible.
 * All members are fictional. Expert authors mirror [ExpertSeed].
 */
object CommunitySeed {

    // ---------------------------------------------------------------- Experts

    private val kavya = CommunityAuthor(
        id = "exp-kavya",
        displayName = "Dr. Kavya Menon",
        isExpert = true,
        credential = "Consultant Dermatologist",
    )
    private val arjun = CommunityAuthor(
        id = "exp-arjun",
        displayName = "Dr. Arjun Deshpande",
        isExpert = true,
        credential = "Cosmetic Dermatologist",
    )
    private val elodie = CommunityAuthor(
        id = "exp-elodie",
        displayName = "Élodie Marchand",
        isExpert = true,
        credential = "Clinical Aesthetician",
    )
    private val nandini = CommunityAuthor(
        id = "exp-nandini",
        displayName = "Nandini Iyer",
        isExpert = true,
        credential = "Skin Nutrition Specialist",
    )
    private val samir = CommunityAuthor(
        id = "exp-samir",
        displayName = "Dr. Samir Haddad",
        isExpert = true,
        credential = "Paediatric & Sensitive Skin Dermatologist",
    )
    private val ishita = CommunityAuthor(
        id = "exp-ishita",
        displayName = "Dr. Ishita Banerjee",
        isExpert = true,
        credential = "Pigmentation Specialist",
    )

    // ---------------------------------------------------------------- Members

    private val meera = CommunityAuthor(id = "usr-meera", displayName = "Meera K.", journeyWeek = 6)
    private val rohan = CommunityAuthor(id = "usr-rohan", displayName = "Rohan S.", journeyWeek = 3)
    private val aisha = CommunityAuthor(id = "usr-aisha", displayName = "Aisha F.", journeyWeek = 9)
    private val tanvi = CommunityAuthor(id = "usr-tanvi", displayName = "Tanvi P.", journeyWeek = 1)
    private val karan = CommunityAuthor(id = "usr-karan", displayName = "Karan M.", journeyWeek = 4)
    private val leela = CommunityAuthor(id = "usr-leela", displayName = "Leela V.", journeyWeek = 8)
    private val sana = CommunityAuthor(id = "usr-sana", displayName = "Sana A.", journeyWeek = 11)
    private val dev = CommunityAuthor(id = "usr-dev", displayName = "Dev R.", journeyWeek = 5)
    private val farah = CommunityAuthor(id = "usr-farah", displayName = "Farah N.", journeyWeek = 7)
    private val ishaan = CommunityAuthor(id = "usr-ishaan", displayName = "Ishaan T.", journeyWeek = 2)
    private val nikhil = CommunityAuthor(id = "usr-nikhil", displayName = "Nikhil J.", journeyWeek = 12)
    private val priyanka = CommunityAuthor(id = "usr-priyanka", displayName = "Priyanka D.", journeyWeek = 10)
    private val zoya = CommunityAuthor(id = "usr-zoya", displayName = "Zoya H.", journeyWeek = 9)
    private val anjali = CommunityAuthor(id = "usr-anjali", displayName = "Anjali B.", journeyWeek = 14)
    private val joel = CommunityAuthor(id = "usr-joel", displayName = "Joel M.", journeyWeek = 6)
    private val ritu = CommunityAuthor(id = "usr-ritu", displayName = "Ritu S.", journeyWeek = 3)

    fun posts(now: Long): List<CommunityPost> {
        fun ago(hours: Long): Long = now - (hours * 3_600_000L)

        return listOf(

            // ------------------------------------------------------------ Questions

            CommunityPost(
                id = "pst-niacinamide-vitamin-c",
                section = CommunitySection.QUESTIONS,
                author = meera,
                title = "Can I use niacinamide and vitamin C in the same routine?",
                body = "My plan has vitamin C in the morning and a niacinamide serum I already own. " +
                    "I keep reading old posts saying they cancel each other out. " +
                    "Is it fine to layer them, or should I split them between morning and evening?",
                tags = listOf("niacinamide", "vitamin c", "layering"),
                createdAt = ago(30),
                likeCount = 84,
                relatedGoals = setOf(SkinGoal.UNEVEN_TONE, SkinGoal.DARK_SPOTS),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-niacinamide-vitamin-c-1",
                        postId = "pst-niacinamide-vitamin-c",
                        author = ishita,
                        body = "Good question, and a very common one. The idea that they cancel out comes from older lab studies " +
                            "using conditions quite different from modern formulas, and most people can use both in the same routine. " +
                            "Apply vitamin C first, let it settle for a minute, then niacinamide, moisturizer and sunscreen. " +
                            "If your skin feels warm or flushed, simply move niacinamide to the evening.",
                        createdAt = ago(26),
                        likeCount = 58,
                        isExpertAnswer = true,
                    ),
                    CommunityComment(
                        id = "cmt-niacinamide-vitamin-c-2",
                        postId = "pst-niacinamide-vitamin-c",
                        author = joel,
                        body = "I split mine for the first month just to see how my skin reacted, then combined them. " +
                            "No issues for me, but splitting first made me feel more confident.",
                        createdAt = ago(20),
                        likeCount = 12,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-first-week-retinal",
                section = CommunitySection.QUESTIONS,
                author = rohan,
                title = "First week with retinal: is a little dryness normal?",
                body = "I started retinal two nights a week, as my plan suggested. " +
                    "Around my nose and chin feels a bit dry and slightly flaky now. " +
                    "Should I push through, or stop?",
                tags = listOf("retinal", "first weeks", "dryness"),
                createdAt = ago(52),
                likeCount = 61,
                relatedGoals = setOf(SkinGoal.TEXTURE, SkinGoal.FINE_LINES),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-first-week-retinal-1",
                        postId = "pst-first-week-retinal",
                        author = arjun,
                        body = "Some mild dryness in the first few weeks is common while skin adjusts, and the nose and mouth " +
                            "corners often feel it first. It helps to apply a thin layer of moisturizer before the retinal on those areas, " +
                            "or to keep it off them for now. If you have stinging, burning or redness that lasts, pause for a few nights " +
                            "and restart more slowly. Please see a dermatologist if irritation is significant or does not settle.",
                        createdAt = ago(47),
                        likeCount = 44,
                        isExpertAnswer = true,
                    ),
                    CommunityComment(
                        id = "cmt-first-week-retinal-2",
                        postId = "pst-first-week-retinal",
                        author = anjali,
                        body = "What worked for me was moisturizer first, retinal on top, for the whole first month. " +
                            "It felt slower, but I never had to take a break.",
                        createdAt = ago(40),
                        likeCount = 19,
                    ),
                    CommunityComment(
                        id = "cmt-first-week-retinal-3",
                        postId = "pst-first-week-retinal",
                        author = rohan,
                        body = "Thank you both. Trying the moisturizer buffer tonight and skipping the corners of my nose.",
                        createdAt = ago(36),
                        likeCount = 6,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-sunscreen-over-makeup",
                section = CommunitySection.QUESTIONS,
                author = aisha,
                title = "How do you reapply sunscreen over makeup?",
                body = "I wear light makeup to work and I know I should reapply at lunch. " +
                    "Rubbing more sunscreen on top ruins everything. " +
                    "What do people actually do?",
                tags = listOf("sunscreen", "reapplication", "makeup"),
                createdAt = ago(5),
                likeCount = 37,
                relatedGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.OVERALL_HEALTH),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-sunscreen-over-makeup-1",
                        postId = "pst-sunscreen-over-makeup",
                        author = elodie,
                        body = "Pressing rather than rubbing is the key. Put a small amount of a fluid sunscreen on a damp makeup sponge " +
                            "and pat it over the face in thin layers. Blot any shine with a tissue first. " +
                            "Sunscreen sprays and powders can be handy top-ups, though it is hard to apply enough of them, " +
                            "so treat them as extras rather than your main layer.",
                        createdAt = ago(3),
                        likeCount = 21,
                        isExpertAnswer = true,
                    ),
                    CommunityComment(
                        id = "cmt-sunscreen-over-makeup-2",
                        postId = "pst-sunscreen-over-makeup",
                        author = priyanka,
                        body = "The sponge trick changed my workdays. I keep a tiny tube and a sponge in a zip pouch in my bag.",
                        createdAt = ago(1),
                        likeCount = 8,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-patch-testing-everything",
                section = CommunitySection.QUESTIONS,
                author = tanvi,
                title = "Do I really need to patch test everything?",
                body = "First week here and my plan suggests patch testing each new product. " +
                    "Even the cleanser? It feels like it will take forever to start.",
                tags = listOf("patch testing", "beginners", "sensitive skin"),
                createdAt = ago(96),
                likeCount = 52,
                relatedGoals = setOf(SkinGoal.REDNESS, SkinGoal.OVERALL_HEALTH),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-patch-testing-everything-1",
                        postId = "pst-patch-testing-everything",
                        author = samir,
                        body = "It is worth it, especially if your skin tends to react. Rinse-off cleansers are lower risk, so a few days " +
                            "on the side of the neck is usually enough. Give leave-on products, and especially actives, five to seven days. " +
                            "You can test two products at once on opposite sides of the neck to save time. " +
                            "If anything itches, burns or swells, stop and ask a dermatologist.",
                        createdAt = ago(90),
                        likeCount = 47,
                        isExpertAnswer = true,
                    ),
                    CommunityComment(
                        id = "cmt-patch-testing-everything-2",
                        postId = "pst-patch-testing-everything",
                        author = zoya,
                        body = "I used to skip it. Then a moisturizer I loved on paper made my cheeks itchy for a week. " +
                            "Patch testing is boring, but so much less boring than that.",
                        createdAt = ago(80),
                        likeCount = 29,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-cleanser-ph",
                section = CommunitySection.QUESTIONS,
                author = karan,
                title = "Does cleanser pH actually matter?",
                body = "I see a lot of posts saying you must use a cleanser with exactly pH 5.5. " +
                    "My current one does not say its pH anywhere. " +
                    "Should I be worried, or is this overthinking it?",
                tags = listOf("ph", "cleansing", "myths"),
                createdAt = ago(140),
                likeCount = 33,
                relatedGoals = setOf(SkinGoal.OVERALL_HEALTH),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-cleanser-ph-1",
                        postId = "pst-cleanser-ph",
                        author = sana,
                        body = "From what I have read here, how your skin feels after washing matters more than the exact number. " +
                            "If it feels tight or squeaky, that is a sign to try something gentler. The Learn article on pH helped me.",
                        createdAt = ago(130),
                        likeCount = 18,
                    ),
                    CommunityComment(
                        id = "cmt-cleanser-ph-2",
                        postId = "pst-cleanser-ph",
                        author = ritu,
                        body = "Same question here. My probe pH readings look similar whichever gentle cleanser I use, " +
                            "but I would ask a dermatologist if you are having any irritation.",
                        createdAt = ago(120),
                        likeCount = 7,
                    ),
                ),
            ),

            // ------------------------------------------------------------ Discussions

            CommunityPost(
                id = "pst-travel-routine",
                section = CommunitySection.DISCUSSIONS,
                author = leela,
                title = "Keeping a routine on a ten-day work trip",
                body = "I am travelling for work next week with a carry-on only. " +
                    "Planning to take cleanser, one serum, moisturizer and sunscreen in small bottles. " +
                    "What do you all pack, and what do you leave behind?",
                tags = listOf("travel", "routine", "minimal"),
                createdAt = ago(20),
                likeCount = 46,
                relatedGoals = setOf(SkinGoal.OVERALL_HEALTH),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-travel-routine-1",
                        postId = "pst-travel-routine",
                        author = nikhil,
                        body = "That is almost exactly my list. I leave actives at home for short trips so I am not " +
                            "dealing with irritation somewhere new.",
                        createdAt = ago(15),
                        likeCount = 14,
                    ),
                    CommunityComment(
                        id = "cmt-travel-routine-2",
                        postId = "pst-travel-routine",
                        author = elodie,
                        body = "A lovely, sensible kit. Hotel air and flights can feel drying, so a slightly richer moisturizer " +
                            "for the evenings is worth the space. Keep sunscreen in your day bag, not your suitcase.",
                        createdAt = ago(12),
                        likeCount = 23,
                    ),
                    CommunityComment(
                        id = "cmt-travel-routine-3",
                        postId = "pst-travel-routine",
                        author = aisha,
                        body = "I decant into labelled mini bottles and set a reminder on my phone at the same time as at home. " +
                            "The routine feels less like a chore that way.",
                        createdAt = ago(10),
                        likeCount = 9,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-gentle-cleansing",
                section = CommunitySection.DISCUSSIONS,
                author = sana,
                title = "Gentle cleansing converts: what changed for you?",
                body = "Eleven weeks ago I swapped a foaming cleanser for a cream one. " +
                    "The biggest change for me is that my skin no longer feels tight by mid-morning. " +
                    "Curious what others noticed after switching.",
                tags = listOf("cleansing", "gentle", "routine"),
                createdAt = ago(72),
                likeCount = 88,
                relatedGoals = setOf(SkinGoal.HYDRATION, SkinGoal.OVERALL_HEALTH),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-gentle-cleansing-1",
                        postId = "pst-gentle-cleansing",
                        author = karan,
                        body = "Similar for me. I also stopped feeling like I needed three layers of moisturizer afterwards.",
                        createdAt = ago(66),
                        likeCount = 11,
                    ),
                    CommunityComment(
                        id = "cmt-gentle-cleansing-2",
                        postId = "pst-gentle-cleansing",
                        author = kavya,
                        body = "It is one of the most underrated changes you can make. A cleanser only needs to remove the day, " +
                            "not everything else with it. That tight, squeaky feeling is usually a sign to go gentler.",
                        createdAt = ago(60),
                        likeCount = 39,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-dark-circles-sleep",
                section = CommunitySection.DISCUSSIONS,
                author = dev,
                title = "Dark circles and sleep: what are you noticing?",
                body = "My under-eye area is a focus area in my SkinPrint. " +
                    "I have been trying to keep a more regular bedtime, mostly because I feel better for it. " +
                    "Has anyone else been tracking this alongside their routine?",
                tags = listOf("dark circles", "sleep", "habits"),
                createdAt = ago(110),
                likeCount = 57,
                relatedGoals = setOf(SkinGoal.DARK_CIRCLES),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-dark-circles-sleep-1",
                        postId = "pst-dark-circles-sleep",
                        author = nandini,
                        body = "A regular rhythm is a lovely thing for general wellbeing. It is worth knowing that dark circles " +
                            "have many contributors, including genetics, pigment and the natural shape of the eye area, " +
                            "so sleep is only one part of the picture. If darkness or puffiness appears suddenly, " +
                            "please check in with a doctor or dermatologist.",
                        createdAt = ago(100),
                        likeCount = 34,
                    ),
                    CommunityComment(
                        id = "cmt-dark-circles-sleep-2",
                        postId = "pst-dark-circles-sleep",
                        author = meera,
                        body = "Mine run in my family, so I have made peace with them. An eye serum and sunscreen right up to " +
                            "the under-eye area help them look softer on camera, which is enough for me.",
                        createdAt = ago(95),
                        likeCount = 21,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-how-few-steps",
                section = CommunitySection.DISCUSSIONS,
                author = farah,
                title = "Minimal routines: how few steps is enough?",
                body = "I have gone from eight products to four and honestly I am enjoying my routine more. " +
                    "Cleanser, niacinamide, moisturizer, sunscreen. " +
                    "Is anyone else doing a stripped-back version?",
                tags = listOf("minimal", "routine", "consistency"),
                createdAt = ago(200),
                likeCount = 112,
                relatedGoals = setOf(SkinGoal.OVERALL_HEALTH, SkinGoal.PORES),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-how-few-steps-1",
                        postId = "pst-how-few-steps",
                        author = kavya,
                        body = "Four well-chosen steps is a very solid routine. The best routine is the one you will do on a " +
                            "tired Tuesday, and shorter routines are much easier to keep.",
                        createdAt = ago(190),
                        likeCount = 52,
                    ),
                    CommunityComment(
                        id = "cmt-how-few-steps-2",
                        postId = "pst-how-few-steps",
                        author = ishaan,
                        body = "Three here: cleanser, moisturizer, sunscreen. Starting small so I actually stick to it.",
                        createdAt = ago(185),
                        likeCount = 15,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-humid-weather-textures",
                section = CommunitySection.DISCUSSIONS,
                author = ishaan,
                title = "Which moisturizer textures do you like in humid weather?",
                body = "Monsoon season has started here and my cream feels heavy by lunchtime. " +
                    "Thinking of trying a gel moisturizer for the day. " +
                    "What textures work for you when it is sticky outside?",
                tags = listOf("moisturizer", "humidity", "texture"),
                createdAt = ago(8),
                likeCount = 24,
                relatedGoals = setOf(SkinGoal.HYDRATION),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-humid-weather-textures-1",
                        postId = "pst-humid-weather-textures",
                        author = aisha,
                        body = "Gel in the morning, cream at night is what worked for me. Skin still feels comfortable " +
                            "without the midday shine.",
                        createdAt = ago(6),
                        likeCount = 10,
                    ),
                    CommunityComment(
                        id = "cmt-humid-weather-textures-2",
                        postId = "pst-humid-weather-textures",
                        author = joel,
                        body = "Same. I also switched to a gel sunscreen, which let me skip moisturizer on the most humid mornings.",
                        createdAt = ago(4),
                        likeCount = 5,
                    ),
                ),
            ),

            // ------------------------------------------------------------ Success stories

            CommunityPost(
                id = "pst-twelve-weeks-consistency",
                section = CommunitySection.SUCCESS_STORIES,
                author = nikhil,
                title = "Twelve weeks of the same simple routine",
                body = "I have done the same five steps, most days, for twelve weeks. " +
                    "No new products, no switching when I got bored. " +
                    "My skin feels calmer and my texture score has crept up steadily. " +
                    "The biggest lesson for me was that boring works.",
                tags = listOf("consistency", "progress", "routine"),
                createdAt = ago(44),
                likeCount = 164,
                relatedGoals = setOf(SkinGoal.TEXTURE, SkinGoal.OVERALL_HEALTH),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-twelve-weeks-consistency-1",
                        postId = "pst-twelve-weeks-consistency",
                        author = tanvi,
                        body = "This is so encouraging to read in my first week. Saving it for when I get impatient.",
                        createdAt = ago(40),
                        likeCount = 17,
                    ),
                    CommunityComment(
                        id = "cmt-twelve-weeks-consistency-2",
                        postId = "pst-twelve-weeks-consistency",
                        author = rohan,
                        body = "How did you keep going on the days you did not feel like it?",
                        createdAt = ago(38),
                        likeCount = 6,
                    ),
                    CommunityComment(
                        id = "cmt-twelve-weeks-consistency-3",
                        postId = "pst-twelve-weeks-consistency",
                        author = nikhil,
                        body = "Products next to my toothbrush, and I let myself do a two-step version when I was exhausted. " +
                            "Something is better than nothing.",
                        createdAt = ago(35),
                        likeCount = 22,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-hydration-index-up",
                section = CommunitySection.SUCCESS_STORIES,
                author = priyanka,
                title = "My hydration index has moved from 41 to 58",
                body = "Ten weeks ago I switched to a cream cleanser and added a hyaluronic serum on damp skin. " +
                    "My probe hydration index has gone from the low 40s to 58 across my last three check-ins. " +
                    "I know readings vary with weather and time of day, so I always measure in the morning before my routine. " +
                    "Mostly, my skin just feels more comfortable.",
                tags = listOf("hydration", "probe", "progress"),
                createdAt = ago(160),
                likeCount = 138,
                relatedGoals = setOf(SkinGoal.HYDRATION),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-hydration-index-up-1",
                        postId = "pst-hydration-index-up",
                        author = elodie,
                        body = "Measuring at the same time each day is exactly the right approach, and a steady trend over " +
                            "several check-ins is far more meaningful than one number. Lovely to see.",
                        createdAt = ago(150),
                        likeCount = 31,
                    ),
                    CommunityComment(
                        id = "cmt-hydration-index-up-2",
                        postId = "pst-hydration-index-up",
                        author = karan,
                        body = "Did you change anything else at the same time? Trying to work out whether my cleanser is the issue.",
                        createdAt = ago(140),
                        likeCount = 4,
                    ),
                    CommunityComment(
                        id = "cmt-hydration-index-up-3",
                        postId = "pst-hydration-index-up",
                        author = priyanka,
                        body = "Only the cleanser for the first four weeks, then the serum. Changing one thing at a time helped me see what was working.",
                        createdAt = ago(136),
                        likeCount = 12,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-stopped-chasing-products",
                section = CommunitySection.SUCCESS_STORIES,
                author = zoya,
                title = "I finally stopped chasing new products",
                body = "I used to buy something new every fortnight and my skin always felt unsettled. " +
                    "Nine weeks ago I committed to my plan and started patch testing properly. " +
                    "My redness looks calmer, I spend less, and I actually enjoy my routine now.",
                tags = listOf("patch testing", "sensitive skin", "mindset"),
                createdAt = ago(250),
                likeCount = 97,
                relatedGoals = setOf(SkinGoal.REDNESS, SkinGoal.OVERALL_HEALTH),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-stopped-chasing-products-1",
                        postId = "pst-stopped-chasing-products",
                        author = samir,
                        body = "Thank you for sharing this. Reactive skin often settles when it is given fewer changes to cope with, " +
                            "and your patience is a big part of that.",
                        createdAt = ago(240),
                        likeCount = 28,
                    ),
                    CommunityComment(
                        id = "cmt-stopped-chasing-products-2",
                        postId = "pst-stopped-chasing-products",
                        author = ritu,
                        body = "Needed to read this. My bathroom shelf is proof I have the same habit.",
                        createdAt = ago(230),
                        likeCount = 13,
                    ),
                ),
            ),

            // ------------------------------------------------------------ Expert corner

            CommunityPost(
                id = "pst-sunscreen-five-things",
                section = CommunitySection.EXPERT_CORNER,
                author = ishita,
                title = "Five things I wish everyone knew about sunscreen",
                body = "One: most people apply about half the amount they need, so aim for two finger-lengths for face and neck. " +
                    "Two: UVA passes through windows, so sunscreen matters on indoor days near glass. " +
                    "Three: reapplying outdoors counts as much as the SPF number. " +
                    "Four: deeper skin tones benefit from sun protection too, especially for dark spots. " +
                    "Five: the best sunscreen is the one you enjoy wearing every day.",
                tags = listOf("sunscreen", "expert tips", "dark spots"),
                createdAt = ago(280),
                likeCount = 236,
                relatedGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE),
                isPinned = true,
                comments = listOf(
                    CommunityComment(
                        id = "cmt-sunscreen-five-things-1",
                        postId = "pst-sunscreen-five-things",
                        author = leela,
                        body = "Number two surprised me. I sit next to a big window at work, so I will start taking this more seriously.",
                        createdAt = ago(270),
                        likeCount = 26,
                    ),
                    CommunityComment(
                        id = "cmt-sunscreen-five-things-2",
                        postId = "pst-sunscreen-five-things",
                        author = ishita,
                        body = "A window seat can mean more daylight on one side of the face, and many people notice their tone " +
                            "looks less even on that side. Your morning layer plus a top-up after lunch is a great start.",
                        createdAt = ago(262),
                        likeCount = 41,
                    ),
                    CommunityComment(
                        id = "cmt-sunscreen-five-things-3",
                        postId = "pst-sunscreen-five-things",
                        author = farah,
                        body = "Two finger-lengths felt like a lot at first, but a lighter fluid made it much easier.",
                        createdAt = ago(250),
                        likeCount = 15,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-starting-retinal-gently",
                section = CommunitySection.EXPERT_CORNER,
                author = arjun,
                title = "Starting retinal gently: a note for new members",
                body = "Many of you are starting retinal this month. Begin with two evenings a week on dry skin, " +
                    "using a pea-sized amount for the whole face. " +
                    "Buffer with moisturizer if you feel dry, and keep it away from the corners of the nose and mouth at first. " +
                    "Increase only when your skin feels comfortable, and wear sunscreen every morning. " +
                    "If you are pregnant, trying to conceive or breastfeeding, please talk to your doctor before using a retinoid.",
                tags = listOf("retinal", "expert tips", "first weeks"),
                createdAt = ago(180),
                likeCount = 149,
                relatedGoals = setOf(SkinGoal.FINE_LINES, SkinGoal.TEXTURE),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-starting-retinal-gently-1",
                        postId = "pst-starting-retinal-gently",
                        author = rohan,
                        body = "This is exactly what I needed before my first week. The buffering tip saved my nose.",
                        createdAt = ago(170),
                        likeCount = 18,
                    ),
                    CommunityComment(
                        id = "cmt-starting-retinal-gently-2",
                        postId = "pst-starting-retinal-gently",
                        author = anjali,
                        body = "Fourteen weeks in and still on three nights a week. Slow and steady has suited me well.",
                        createdAt = ago(160),
                        likeCount = 11,
                    ),
                ),
            ),

            CommunityPost(
                id = "pst-food-and-skin",
                section = CommunitySection.EXPERT_CORNER,
                author = nandini,
                title = "Food and skin: keeping expectations realistic",
                body = "I am often asked which foods are good for skin. Balanced meals, enough water and regular routines " +
                    "support general wellbeing, and skin is part of that. " +
                    "The research linking specific foods to specific skin changes is still developing and varies from person to person. " +
                    "If you notice a pattern, keep a simple note in your journal and discuss it with a dermatologist or nutrition professional " +
                    "rather than cutting out whole food groups.",
                tags = listOf("nourishment", "wellbeing", "expert tips"),
                createdAt = ago(230),
                likeCount = 118,
                relatedGoals = setOf(SkinGoal.OVERALL_HEALTH),
                comments = listOf(
                    CommunityComment(
                        id = "cmt-food-and-skin-1",
                        postId = "pst-food-and-skin",
                        author = dev,
                        body = "Refreshing to read something that is not a list of forbidden foods. Thank you.",
                        createdAt = ago(220),
                        likeCount = 24,
                    ),
                    CommunityComment(
                        id = "cmt-food-and-skin-2",
                        postId = "pst-food-and-skin",
                        author = priyanka,
                        body = "The journal idea is helpful. I would rather notice patterns calmly than guess.",
                        createdAt = ago(215),
                        likeCount = 9,
                    ),
                ),
            ),
        )
    }
}
