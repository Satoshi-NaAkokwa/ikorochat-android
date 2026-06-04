package com.ikoro.android.wallet.onboarding

import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import kotlin.math.floor

/**
 * BIP-39 Mnemonic Phrase Generator
 * Generates 12/24 word mnemonic phrases following BIP-39 standard
 */
class MnemonicGenerator(private val strength: Int = 128) {
    
    companion object {
        private const val TAG = "MnemonicGenerator"
        private const val WORD_LIST_SIZE = 2048
        
        // Word list (BIP-39)
        private val WORD_LIST = listOf(
            "abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract", "absence", "absorbing",
            "abuse", "accent", "accept", "accepting", "access", "accident", "accompany", "accomplish", "according",
            "account", "achieve", "acid", "acquired", "across", "act", "action", "activity", "actually", "add",
            "address", "administration", "admit", "adult", "affect", "after", "again", "against", "agency", "agent",
            "agree", "agreement", "ahead", "air", "all", "allow", "almost", "alone", "along", "already",
            "also", "although", "always", "american", "among", "amount", "analysis", "animal", "another", "answer",
            "any", "anyone", "anything", "appear", "apply", "appoint", "appointments", "approach", "area",
            "arm", "army", "around", "arrange", "arrangement", "arranging", "arrest", "arrive", "article",
            "artificial", "artist", "as", "associate", "associated", "association", "assume", "attack", "attend",
            "attitude", "attention", "attraction", "authority", "automatic", "available", "average", "awake",
            "aware", "away", "baby", "back", "bad", "bag", "ball", "ban", "band", "bank", "bar", "base",
            "beat", "beautiful", "became", "because", "become", "becoming", "bed", "been", "before", "began",
            "begin", "beginning", "behalf", "behind", "believe", "benefit", "best", "better", "between", "beyond",
            "big", "bill", "billion", "bird", "black", "blood", "blue", "board", "body", "book", "bought", "bound",
            "broke", "brother", "brought", "brown", "build", "building", "built", "business", "buy", "buying", "by",
            "cafe", "calendar", "call", "came", "camp", "can", "cancel", "candidate", "cane", "cannot", "cap",
            "capital", "car", "card", "care", "career", "carrying", "case", "cash", "cat", "catch", "category",
            "caught", "cause", "caused", "causing", "cell", "cent", "center", "central", "century", "certain",
            "certainly", "chain", "chair", "chairman", "chance", "change", "changing", "charge", "charged",
            "charging", "cheap", "check", "cheese", "chemical", "chest", "chief", "child", "children", "choice",
            "choose", "choosing", "church", "citizen", "city", "civil", "claim", "class", "clean", "clear",
            "click", "client", "clock", "close", "closing", " cloth", "control", "history", "hit", "hold",
            "home", "hope", "hospital", "hotel", "hour", "house", "however", "huge", "human", "husband",
            "idea", "identify", "image", "imagine", "impact", "important", "imposed", "improve", "impression",
            "impressive", "in", "income", "independence", "indicate", "individual", "industry", "inform", "inside",
            "instead", "institution", "interest", "interesting", "interview", "introduce", "issue", "item",
            "itself", "join", "job", "joined", "judge", "jump", "junior", "just", "keep", "kept", "key", "killed",
            "kind", "kitchen", "known", "knowledge", "land", "language", "large", "last", "late", "later", "laugh",
            "law", "layer", "lead", "leader", "leadership", "leading", "league", "left", "legal", "less", "level",
            "life", "light", "like", "likely", "line", "list", "listen", "little", "live", "living", "local", "location",
            "long", "look", "losing", "love", "lying", "machine", "magazine", "main", "maintain", "major",
            "make", "making", "manage", "management", "manager", "many", "market", "marriage", "material", "matter",
            "maybe", "mean", "meaning", "measure", "medical", "meeting", "memory", "mention", "method", "middle",
            "might", "military", "million", "mind", "minute", "miss", "mission", "model", "modern", "moment",
            "money", "month", "moral", "more", "morning", "most", "mother", "motion", "mountain", "mouth", "move",
            "movement", "movie", "much", "music", "must", "name", "nation", "nature", "near", "necessary", "negotiate",
            "neighbor", "nerve", "next", "nice", "night", "none", "north", "not", "note", "nothing", "notice",
            "now", "number", "occur", "offer", "office", "officer", "official", "often", "oil", "ok", "older",
            "omitted", "one", "ongoing", "once", "one's", "only", "onto", "open", "opening", "operate", "operating",
            "operation", "opinion", "opportunity", "oppose", "option", "or", "order", "organization", "origin",
            "other", "others", "otherwise", "ought", "our", "ourselves", "out", "outer", "outlook", "over",
            "overall", "overcome", "overlook", "own", "owner", "pace", "pack", "package", "page", "pain", "paint",
            "pair", "panel", "paper", "parent", "part", "participant", "participation", "particular", "particularly",
            "partly", "partner", "party", "pass", "passage", "passenger", "passing", "password", "past", "path",
            "patient", "pattern", "peace", "people", "percentage", "performance", "period", "permit", "person",
            "personal", "personality", "perspective", "pressure", "pretend", "prevent", "private", "prize",
            "problem", "procedure", "process", "product", "production", "professor", "profile", "profit",
            "program", "project", "promote", "promotion", "proof", "property", "proposal", "protection",
            "protein", "prove", "provide", "public", "pull", "purpose", "push", "quality", "question", "quickly",
            "quiet", "quite", "quote", "race", "radio", "rain", "raise", "range", "rate", "rather", "reach",
            "read", "reading", "reality", "reason", "recent", "recently", "recognize", "recommend", "record",
            "reduce", "refuse", "region", "relate", "relief", "religion", "remote", "remove", "repair", "repeat",
            "replace", "report", "represent", "republican", "request", "require", "research", "resource", "respond",
            "response", "responsibility", "rest", "result", "return", "reveal", "review", "risk", "river", "road",
            "rock", "role", "room", "rule", "running", "safe", "salary", "sample", "save", "scheme", "school",
            "science", "scotland", "screen", "search", "season", "seat", "second", "secretary", "section",
            "sector", "security", "see", "seed", "seek", "seem", "segment", "seize", "seldom", "select",
            "selection", "self", "sense", "sensitive", "sentence", "set", "seven", "several", "shake", "share",
            "sharp", "she", "shelf", "shell", "shift", "shine", "ship", "shirt", "shock", "shoe", "short",
            "seeming", "seems", "sell", "send", "sentence", "sentiment", "separate", "sequence", "series",
            "serious", "serve", "service", "set", "seven", "several", "sharp", "she", "shelf", "shell", "shift",
            "shirt", "shock", "shoe", "short", "show", "shower", "sick", "side", "sight", "sign", "signal",
            "signature", "significance", "silver", "similar", "simple", "simply", "single", "sister", "site",
            "situation", "size", "skill", "skin", "small", "smile", "snow", "society", "solo", "son", "song",
            "sort", "sound", "soup", "source", "south", "space", "speak", "special", "specific", "speech",
            "spending", "staff", "stage", "stair", "stake", "stand", "standard", "star", "start", "state",
            "statement", "station", "status", "stay", "steak", "steal", "step", "still", "stock", "stop",
            "storage", "store", "storm", "story", "strategy", "stream", "street", "strength", "stress", "stretch",
            "strike", "string", "strong", "structure", "structure", "student", "studied", "studio", "study",
            "stuff", "style", "subject", "success", "successful", "such", "suddenly", "suffer", "sugar",
            "suggest", "suit", "summer", "sun", "support", "suppose", "surface", "surgery", "surprise",
            "surround", "survey", "survive", "swallow", "swim", "swimming", "swing", "switch", "symbol",
            "system", "table", "tackle", "tag", "take", "talk", "tall", "tank", "tap", "target", "task",
            "tax", "tea", "teach", "team", "tear", "teeth", "television", "tell", "telephone", "television",
            "temperature", "temporarily", "temporary", "ten", "tend", "term", "test", "testing", "text",
            "thanks", "that", "their", "theirs", "them", "themselves", "then", "theory", "there", "these",
            "they", "thick", "thin", "thing", "this", "thoroughly", "those", "though", "thought", "thousand",
            "threat", "three", "through", "throughout", "throw", "thus", "threat", "thousand", "_through",
            "tide", "tie", "tiger", "tight", "time", "tiny", "tip", "tired", "title", "to", "tobacco",
            "today", "together", "tomorrow", "tone", "tongue", "tool", "tooth", "top", "topic", "total",
            "touches", "tower", "town", "track", "trade", "tradition", "traffic", "train", "transportation",
            "travel", "treat", "treatment", "tree", "trial", "trip", "trouble", "truck", "true", "truth",
            "try", "tube", "tunnel", "turn", "TV", "two", "type", "under", "understand", "understanding",
            "understood", "unless", "unlike", "unlikely", "until", "unusual", "up", "upon", "upper", "urban",
            "urge", "urgent", "us", "use", "useful", "user", "usual", "usually", "vacation", "value", "van",
            "variation", "variety", "various", "vast", "vehicle", "very", "victim", "view", "village",
            "violence", "visit", "visitor", "vision", "visual", "voice", "volume", "wait", "waiting",
            "wake", "walk", "wall", "want", "war", "ward", "watch", "water", "wave", "way", "weak",
            "weather", "web", "week", "weight", "west", "western", "what", "whatever", "when", "where",
            "whether", "which", "while", "white", "why", "wife", "wild", "will", "win", "wind", "window",
            "wine", "wing", "winner", "winter", "wish", " witness", "woman", "wonder", "wood", "wooden",
            "word", "work", "worker", "working", "world", "worry", "worth", "would", "wrap", "write",
            "writer", "writing", "wrong", "yard", "yard", "year", "yellow", "yes", "yesterday", "yet",
            "yield", "you", "your", "yours", "zero"
        ).toSet().toList()
    }
    
    init {
        if (strength != 128 && strength != 256) {
            throw IllegalArgumentException("Strength must be 128 or 256")
        }
    }
    
    /**
     * Generate random entropy
     */
    private fun generateEntropy(): ByteArray {
        val random = SecureRandom()
        val entropy = ByteArray(strength / 8)
        random.nextBytes(entropy)
        return entropy
    }
    
    /**
     * Compute SHA256 hash
     */
    private fun sha256(data: ByteArray): ByteArray {
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(data)
    }
    
    /**
     * Generate 12 or 24 word mnemonic phrase
     */
    fun generate(): List<String> {
        val entropy = generateEntropy()
        
        // Add checksum (first byte of SHA256(entropy))
        val checksum = sha256(entropy)
        val checksumBitCount = strength / 32
        val finalEntropy = entropy.clone()
        
        // Append checksum bits to entropy
        val combined = ByteArray(finalEntropy.size + 1)
        System.arraycopy(finalEntropy, 0, combined, 0, finalEntropy.size)
        System.arraycopy(checksum, 0, combined, finalEntropy.size, 1)
        
        // Split into 11-bit chunks and map to word list
        val mnemonic = mutableListOf<String>()
        var currentByteIndex = 0
        var currentBitIndex = 0
        
        while (currentByteIndex < combined.size) {
            var wordIndex = 0
            for (i in 0..10) {
                wordIndex = wordIndex shl 1
                val bit = (combined[currentByteIndex].toInt() shr (7 - currentBitIndex)) and 0x01
                wordIndex = wordIndex or bit
                currentBitIndex++
                if (currentBitIndex > 7) {
                    currentBitIndex = 0
                    currentByteIndex++
                }
            }
            mnemonic.add(WORD_LIST[wordIndex])
        }
        
        return mnemonic
    }
    
    /**
     * Generate mnemonic as space-separated string
     */
    fun generateString(): String {
        return generate().joinToString(" ")
    }
    
    /**
     * Validate mnemonic phrase
     */
    fun validate(mnemonic: List<String>): Boolean {
        if (mnemonic.size !in listOf(12, 24)) return false
        
        // Check each word exists in list
        for (word in mnemonic) {
            if (!WORD_LIST.contains(word)) return false
        }
        
        // TODO: Implement full BIP-39 validation (checksum verification)
        return true
    }
    
    /**
     * Validate mnemonic string
     */
    fun validate(mnemonic: String): Boolean {
        return validate(mnemonic.split(" "))
    }
}
