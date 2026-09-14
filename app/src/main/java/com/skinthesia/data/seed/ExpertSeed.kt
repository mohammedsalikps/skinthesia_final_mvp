package com.skinthesia.data.seed

import com.skinthesia.domain.model.ConsultationExpert
import com.skinthesia.domain.model.ConsultationType
import com.skinthesia.domain.model.ExpertReview
import com.skinthesia.domain.model.SkinGoal

/**
 * Consultation experts. All people are fictional. Profiles describe general
 * skincare expertise; consultations are simulated in the demonstration build.
 */
object ExpertSeed {

    val experts: List<ConsultationExpert> = listOf(

        ConsultationExpert(
            id = "exp-kavya",
            name = "Dr. Kavya Menon",
            title = "Consultant Dermatologist",
            credentials = "MD Dermatology · 12 years",
            specialties = listOf("Acne-prone skin", "Barrier care", "Simplifying routines"),
            focusGoals = setOf(SkinGoal.ACNE, SkinGoal.PORES, SkinGoal.TEXTURE, SkinGoal.REDNESS),
            yearsExperience = 12,
            rating = 4.9f,
            reviewCount = 540,
            languages = listOf("English", "Malayalam", "Hindi"),
            bio = "Kavya practises general and medical dermatology, with a particular interest in breakout-prone skin. " +
                "She believes most routines improve when they get shorter, not longer. " +
                "Outside clinic she writes plain-language guides for people starting out.",
            approach = "I start by understanding what your skin does on an ordinary day. " +
                "Then we build the smallest routine that fits your life and review it after a few weeks.",
            consultationTypes = setOf(ConsultationType.VIDEO, ConsultationType.CHAT, ConsultationType.SKINPRINT_REVIEW),
            fee = 1800,
            reviews = listOf(
                ExpertReview(
                    author = "Ananya R.",
                    rating = 5,
                    text = "She cut my routine from seven steps to three and explained why each one stayed. " +
                        "I finally feel like I understand what I am putting on my skin.",
                ),
                ExpertReview(
                    author = "Harsh V.",
                    rating = 5,
                    text = "Calm, unhurried and very clear. She was honest that changes would take weeks, which I appreciated.",
                ),
                ExpertReview(
                    author = "Diya S.",
                    rating = 4,
                    text = "Helpful video call with practical next steps. I would have liked a written summary, " +
                        "but the chat follow-up covered it.",
                ),
            ),
        ),

        ConsultationExpert(
            id = "exp-arjun",
            name = "Dr. Arjun Deshpande",
            title = "Cosmetic Dermatologist",
            credentials = "MD Dermatology · 9 years",
            specialties = listOf("Retinoid planning", "Texture and fine lines", "Sun-aware routines"),
            focusGoals = setOf(SkinGoal.FINE_LINES, SkinGoal.TEXTURE, SkinGoal.DARK_SPOTS),
            yearsExperience = 9,
            rating = 4.8f,
            reviewCount = 410,
            languages = listOf("English", "Marathi", "Hindi"),
            bio = "Arjun focuses on cosmetic dermatology and the careful introduction of active ingredients. " +
                "He is known for slow, well-paced plans that respect how skin adjusts. " +
                "He has a soft spot for a well-written sunscreen label.",
            approach = "I start with what your skin tolerates today, then introduce one active at a time. " +
                "We only move faster when your skin tells us it is ready.",
            consultationTypes = setOf(ConsultationType.VIDEO, ConsultationType.SKINPRINT_REVIEW),
            fee = 2200,
            reviews = listOf(
                ExpertReview(
                    author = "Neha G.",
                    rating = 5,
                    text = "He gave me a week-by-week plan for starting retinal and it made the whole thing feel manageable.",
                ),
                ExpertReview(
                    author = "Vikram P.",
                    rating = 5,
                    text = "Thorough SkinPrint review with notes on each area. " +
                        "He also told me which of my products I could simply stop buying.",
                ),
                ExpertReview(
                    author = "Sofia L.",
                    rating = 4,
                    text = "Very knowledgeable and precise. The call felt a little brief, but everything he said was useful.",
                ),
            ),
        ),

        ConsultationExpert(
            id = "exp-elodie",
            name = "Élodie Marchand",
            title = "Clinical Aesthetician",
            credentials = "Advanced Diploma in Clinical Skin Therapy · 14 years",
            specialties = listOf("Hydration and barrier", "Texture and pores", "Practical daily routines"),
            focusGoals = setOf(SkinGoal.HYDRATION, SkinGoal.PORES, SkinGoal.TEXTURE, SkinGoal.OVERALL_HEALTH),
            yearsExperience = 14,
            rating = 4.7f,
            reviewCount = 320,
            languages = listOf("English", "French"),
            bio = "Élodie has spent fourteen years in clinic treatment rooms, working alongside dermatologists. " +
                "She helps people choose textures, order their steps and keep a routine going when life gets busy. " +
                "She refers on to a dermatologist whenever something needs a medical opinion.",
            approach = "I start by asking how your skin feels at noon and at bedtime. " +
                "From there, we adjust textures and steps until the routine feels easy to keep.",
            consultationTypes = setOf(ConsultationType.CHAT, ConsultationType.SKINPRINT_REVIEW),
            fee = 950,
            reviews = listOf(
                ExpertReview(
                    author = "Meghna T.",
                    rating = 5,
                    text = "She noticed I was layering three hydrating products that did the same job. " +
                        "Simpler now, and my skin feels more comfortable.",
                ),
                ExpertReview(
                    author = "Omar K.",
                    rating = 5,
                    text = "Great chat consultation. Quick, warm replies and a routine that works with my travel schedule.",
                ),
                ExpertReview(
                    author = "Pooja N.",
                    rating = 4,
                    text = "Practical and kind. She was clear about what she could advise on and suggested a dermatologist for the rest.",
                ),
            ),
        ),

        ConsultationExpert(
            id = "exp-nandini",
            name = "Nandini Iyer",
            title = "Skin Nutrition Specialist",
            credentials = "MSc Clinical Nutrition · 7 years",
            specialties = listOf("Nourishment and wellbeing", "Hydration habits", "Sustainable daily rhythms"),
            focusGoals = setOf(SkinGoal.OVERALL_HEALTH, SkinGoal.HYDRATION),
            yearsExperience = 7,
            rating = 4.6f,
            reviewCount = 180,
            languages = listOf("English", "Tamil", "Hindi"),
            bio = "Nandini is a nutrition professional who looks at the everyday habits around a skincare routine. " +
                "She does not diagnose or treat skin conditions, and works alongside dermatologists when questions become medical. " +
                "Her sessions focus on realistic, balanced changes rather than strict rules.",
            approach = "I start by looking at your week as it really is: meals, water, sleep and stress. " +
                "Then we choose one or two small habits that support your general wellbeing.",
            consultationTypes = setOf(ConsultationType.VIDEO, ConsultationType.CHAT),
            fee = 800,
            reviews = listOf(
                ExpertReview(
                    author = "Ritika M.",
                    rating = 5,
                    text = "No food lists, no guilt. Just two small changes I could actually keep up with during exams.",
                ),
                ExpertReview(
                    author = "Aditya B.",
                    rating = 4,
                    text = "Thoughtful and grounded. She was upfront that food is one part of the picture, not a quick answer.",
                ),
                ExpertReview(
                    author = "Leena J.",
                    rating = 5,
                    text = "I came in wanting a miracle diet and left with a calmer, more realistic plan. Exactly what I needed.",
                ),
            ),
        ),

        ConsultationExpert(
            id = "exp-samir",
            name = "Dr. Samir Haddad",
            title = "Paediatric & Sensitive Skin Dermatologist",
            credentials = "MD Dermatology, Fellowship in Paediatric Dermatology · 18 years",
            specialties = listOf("Sensitive and reactive skin", "Gentle routines for teens", "Patch testing guidance"),
            focusGoals = setOf(SkinGoal.REDNESS, SkinGoal.HYDRATION, SkinGoal.OVERALL_HEALTH),
            yearsExperience = 18,
            rating = 4.95f,
            reviewCount = 260,
            languages = listOf("English", "Arabic", "French"),
            bio = "Samir has cared for sensitive skin in children, teenagers and adults for eighteen years. " +
                "He is careful, methodical and a firm believer in patch testing. " +
                "Families often come to him when they are unsure which products are gentle enough.",
            approach = "I start by removing anything that might be irritating and letting skin settle. " +
                "Then we reintroduce products slowly, one at a time, so we learn what suits you.",
            consultationTypes = setOf(ConsultationType.VIDEO, ConsultationType.CHAT, ConsultationType.SKINPRINT_REVIEW),
            fee = 2500,
            reviews = listOf(
                ExpertReview(
                    author = "Fatima A.",
                    rating = 5,
                    text = "He helped us put together a very simple routine for my teenage son, who now actually uses it.",
                ),
                ExpertReview(
                    author = "Claire D.",
                    rating = 5,
                    text = "Patient and precise. His patch testing plan took the guesswork out of trying new products.",
                ),
                ExpertReview(
                    author = "Rahul I.",
                    rating = 5,
                    text = "Reassuring from start to finish. He explained what to watch for and when to check back in.",
                ),
            ),
        ),

        ConsultationExpert(
            id = "exp-ishita",
            name = "Dr. Ishita Banerjee",
            title = "Pigmentation Specialist",
            credentials = "MD Dermatology · 11 years",
            specialties = listOf("Pigmentation in deeper skin tones", "Dark spots and uneven tone", "Sun protection planning"),
            focusGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE, SkinGoal.DARK_CIRCLES),
            yearsExperience = 11,
            rating = 4.85f,
            reviewCount = 470,
            languages = listOf("English", "Hindi", "Bengali"),
            bio = "Ishita is a dermatologist with a focus on pigmentation, especially in medium and deeper skin tones. " +
                "She pairs tone-evening ingredients with realistic sun protection habits. " +
                "She is candid about timelines, which are usually measured in months.",
            approach = "I start with your sun habits, because they shape everything else. " +
                "Then we choose one or two tone-evening ingredients and give them time to work.",
            consultationTypes = setOf(ConsultationType.VIDEO, ConsultationType.SKINPRINT_REVIEW),
            fee = 2000,
            reviews = listOf(
                ExpertReview(
                    author = "Tara S.",
                    rating = 5,
                    text = "She explained why my marks were lingering and set expectations kindly. " +
                        "I reapply sunscreen properly now.",
                ),
                ExpertReview(
                    author = "Kabir M.",
                    rating = 4,
                    text = "Clear, direct advice on which serum to keep and which to drop. Worth the fee.",
                ),
                ExpertReview(
                    author = "Nisha P.",
                    rating = 5,
                    text = "Her SkinPrint review was detailed and easy to follow. I liked that she marked what to check again in eight weeks.",
                ),
            ),
        ),
    )
}
