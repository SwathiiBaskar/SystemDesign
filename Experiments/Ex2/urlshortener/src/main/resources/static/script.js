const button = document.getElementById("shortenBtn");

const input = document.getElementById("longURL");

const resultCard = document.getElementById("resultCard");

const shortLink = document.getElementById("shortLink");

button.addEventListener("click", async () => {

    const longURL = input.value.trim();

    if (longURL === "") {

        alert("Please enter a URL.");

        return;

    }

    try {

        const response = await fetch("/shorten", {

            method: "POST",

            headers: {

                "Content-Type": "application/json"

            },

            body: JSON.stringify({

                longURL: longURL

            })

        });

        if (!response.ok) {

            throw new Error();

        }

        const data = await response.json();

        resultCard.classList.remove("hidden");

        shortLink.href = data.shortURL;

        shortLink.innerText = data.shortURL;

    }

    catch (error) {

        alert("Unable to shorten the URL.");

    }

});