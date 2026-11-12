/**
 * Dino English Client JavaScript
 */

// Web Speech API - Text to Speech for English pronunciation
function speakWord(text, lang = 'en-US') {
    if (!('speechSynthesis' in window)) {
        alert("Trình duyệt của bạn không hỗ trợ phát âm tự động!");
        return;
    }

    window.speechSynthesis.cancel(); // Stop any pending speech

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = lang;
    utterance.rate = 0.85; // Slightly slower for clear English learners' comprehension
    utterance.pitch = 1.0;

    // Pick a natural US English voice if available
    const voices = window.speechSynthesis.getVoices();
    const enVoice = voices.find(v => v.lang.startsWith('en') && (v.name.includes('Natural') || v.name.includes('Google') || v.name.includes('English')));
    if (enVoice) {
        utterance.voice = enVoice;
    }

    window.speechSynthesis.speak(utterance);
}

// Flashcard Flip Interaction
function toggleFlashcard(cardElement) {
    if (cardElement) {
        cardElement.classList.toggle('flipped');
    }
}

// Initialize components when DOM is ready
document.addEventListener('DOMContentLoaded', function () {
    // Flashcards click binding
    const flashcards = document.querySelectorAll('.flashcard');
    flashcards.forEach(card => {
        card.addEventListener('click', function (e) {
            // Prevent flip if clicking speaker button
            if (e.target.closest('.btn-speaker')) return;
            toggleFlashcard(this);
        });
    });

    // Quiz radio selection highlight
    const quizOptions = document.querySelectorAll('.quiz-option-card');
    quizOptions.forEach(opt => {
        opt.addEventListener('click', function () {
            const radio = this.querySelector('input[type="radio"]');
            if (radio) {
                radio.checked = true;
                const groupName = radio.getAttribute('name');
                document.querySelectorAll(`input[name="${groupName}"]`).forEach(r => {
                    const parentCard = r.closest('.quiz-option-card');
                    if (parentCard) parentCard.classList.remove('selected');
                });
                this.classList.add('selected');
            }
        });
    });

    // Auto-dismiss alerts after 5s
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });
});
