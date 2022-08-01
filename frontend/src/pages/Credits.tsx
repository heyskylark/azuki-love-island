import { useEffect } from "react";
import Footer from "../components/Footer";

function Credits() {
    useEffect(() => {
        document.title = "Credits / Azuki Love Island"
    }, [])

    return (
        <>
        <div className='container mx-auto'>
            <div className="duration-300 min-h-screen bg-white">
                <main className="max-w-11xl mx-auto mb-8 lg:mb-20 px-4 sm:px-6 lg:px-8">
                    <div className="w-full pt-28">
                        <div>
                            <div className="mb-4 pb-4 border-b-2 border-gray-100">
                                <h1 className="uppercase font-black text-4xl lg:text-5xl tracking-tight whitespace-pre-line">Credits <span className="opacity-10"> //</span></h1>
                            </div>
                            <div className="mb-6">
                                <h1 className="uppercase font-black text-xl lg:text-2xl tracking-tight whitespace-pre-line">HeySkylark</h1>
                                <p className="font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4">Creator/Developer.</p>
                                <a className="font-mono underline text-gray-500 hover:text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4" href="https://twitter.com/heyskylark" target="_blank" rel="noreferrer">@heyskylark.</a>
                            </div>

                            <div className="mb-6">
                                <h1 className="uppercase font-black text-xl lg:text-2xl tracking-tight whitespace-pre-line">Aetherfloweth</h1>
                                <p className="font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4">Creator/Commissioner.</p>
                                <a className="font-mono underline text-gray-500 hover:text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4" href="https://twitter.com/aetherfloweth" target="_blank" rel="noreferrer">@aetherfloweth.</a>
                            </div>

                            <div>
                                <h1 className="uppercase font-black text-xl lg:text-2xl tracking-tight whitespace-pre-line">LeviNotAckerman</h1>
                                <p className="font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4">The Artist behind the Azuki Love Island Voters and Participant POAP.</p>
                                <a className="font-mono underline text-gray-500 hover:text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4" href="https://twitter.com/LeviNotAckerman" target="_blank" rel="noreferrer">@LeviNotAckerman.</a>
                            </div>

                            <div className="mb-6">
                                <h1 className="uppercase font-black text-xl lg:text-2xl tracking-tight whitespace-pre-line">FastandLucid</h1>
                                <p className="font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4">Artist and creator of the beach Azuki Love Island Beanz.</p>
                                <a className="font-mono underline text-gray-500 hover:text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4" href="https://twitter.com/FastandLucid" target="_blank" rel="noreferrer">@FastandLucid.</a>
                            </div>

                            <div className="mb-6">
                                <h1 className="uppercase font-black text-xl lg:text-2xl tracking-tight whitespace-pre-line">UberFlux</h1>
                                <p className="font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4">Blacksmith for the Azuki Love Island Golden Bobu Trophy.</p>
                                <a className="font-mono underline text-gray-500 hover:text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4" href="https://twitter.com/UberFlux" target="_blank" rel="noreferrer">@UberFlux.</a>
                            </div>

                            <div className="mb-6">
                                <h1 className="uppercase font-black text-xl lg:text-2xl tracking-tight whitespace-pre-line">Tsar_Bizarre</h1>
                                <p className="font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4">Auzki Love Island Video Creation/Editing.</p>
                                <a className="font-mono underline text-gray-500 hover:text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4" href="https://twitter.com/tsar_bizarre" target="_blank" rel="noreferrer">@Tsar_Bizarre.</a>
                            </div>

                            <div className="mb-6">
                                <h1 className="uppercase font-black text-xl lg:text-2xl tracking-tight whitespace-pre-line">Keeks</h1>
                                <p className="font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4">Creator of Love Island Season 2 Logo.</p>
                                <a className="font-mono underline text-gray-500 hover:text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4" href="https://twitter.com/its_kiki_hehe" target="_blank" rel="noreferrer">@its_kiki_hehe.</a>
                            </div>

                            <div>
                                <h1 className="uppercase font-black text-xl lg:text-2xl tracking-tight whitespace-pre-line">AzukiOfficial</h1>
                                <p className="font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4">Inspired off the characters and world of Azuki.</p>
                                <a className="font-mono underline text-gray-500 hover:text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4" href="https://twitter.com/AzukiOfficial" target="_blank" rel="noreferrer">@AzukiOfficial.</a>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <Footer footerType={2} />
        </>
    );
}

export default Credits;
