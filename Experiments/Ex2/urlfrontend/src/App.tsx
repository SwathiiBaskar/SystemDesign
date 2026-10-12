import './App.css'
import { useState } from "react";


export default function App() {
    const [longURL, setLongURL] = useState("");
    const [shortURL, setShortURL] = useState("");
    const [loading, setLoading] = useState(false);
    const [copied, setCopied] = useState(false);

    async function shortenURL() {
        if (longURL.trim() === "") {
            alert("Please enter a URL.");
            return;
        }

        setLoading(true);

        try {
            const response = await fetch("http://localhost:8080/shorten", {method: "POST", headers: {"Content-Type": "application/json"},
                body: JSON.stringify({longUrl: longURL})
            });
            const data = await response.json();
            setShortURL(data.shortUrl);
        }catch {
            alert("Unable to connect to the server.");
        }

        setLoading(false);
    }

    async function copyURL() {
        await navigator.clipboard.writeText(shortURL);
        setCopied(true);
        setTimeout(() => {
            setCopied(false);
        }, 2000);
    }

    return (
        <div className="min-h-screen bg-orange-50 flex items-center justify-center px-6">
            <div className="w-full max-w-3xl">
                <div className="text-center">
                    <h1 className="text-5xl font-extrabold text-slate-800">TinyTrail</h1>
                    <p className="text-lg text-emerald-600 font-semibold mt-2">Instant URL Shortener</p>
                    <h2 className="text-2xl font-bold mt-5 text-slate-700">Small links. Big reach.</h2>
                    <p className="text-slate-500 mt-3 max-w-xl mx-auto">
                        Transform lengthy URLs into compact, shareable links
                        in seconds.
                    </p>
                </div>

                <div className="bg-white mt-10 rounded-3xl shadow-xl p-8">
                    <label className="font-semibold text-slate-700">Long URL</label>
                    <div className="mt-2 flex items-center border-2 border-slate-200 rounded-xl px-4 py-3 focus-within:border-emerald-500 transition">
                        <span className="text-xl mr-3">                            🔗
                        </span>

                        <input type="text" value={longURL} onChange={(e) => setLongURL(e.target.value)}
                            placeholder="https://www.google.com/search?q=system+design"
                            className="w-full outline-none"
                        />
                    </div>

                    <button onClick={shortenURL} disabled={loading}
                        className="w-full mt-6 bg-emerald-500 hover:bg-emerald-600 disabled:bg-emerald-300 text-white font-semibold py-3 rounded-xl transition"
                    >
                        {loading ? "Generating..." : " Create Short URL"}
                    </button>

                    {
                        shortURL &&
                        <div className="mt-10 bg-emerald-50 border border-emerald-200 rounded-2xl p-6">
                            <h3 className="font-bold text-emerald-700 text-lg">✨ Your Tiny Link</h3>
                            <div className="mt-4 flex flex-col sm:flex-row gap-3">
                                <a href={shortURL} target="_blank" className="flex-1 break-all text-blue-700 hover:underline font-medium">
                                    {shortURL}
                                </a>

                                <button onClick={copyURL} className="bg-white border border-slate-300 hover:bg-slate-100 px-5 py-2 rounded-lg">
                                    {copied ? "✅ Copied" : "📋 Copy"}
                                </button>
                            </div>
                        </div>
                    }
                </div>

            </div>
        </div>
    );

}