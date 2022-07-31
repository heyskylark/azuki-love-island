import { AxiosError } from "axios";
import { useEffect, useState } from "react";
import { createRenderer } from "react-dom/test-utils";
import { toast } from "react-toastify";
import { getDailyDramaTweets } from "../clients/MainClient";
import Footer from "../components/Footer";
import Loading, { SmallLoading } from "../components/Loading";
import TweetCard from "../components/TweetCard";
import IslandTweets from "../models/api/IslandTweetsResponse";

function Drama(): JSX.Element {
    const MAX_TWEETS = 6
    const [loading, setLoading] = useState<boolean>(true);
    const [loadingMoreTweets, setLoadingMoreTweets] = useState<boolean>(false);
    const [tweets, setTweets] = useState<IslandTweets[]>([]);
    const [tweetPage, setTweetPage] = useState<number>(0);

    useEffect(() => {
        async function init() {
            try {
                const tweetsResponse = await getDailyDramaTweets();
                const tweetData = tweetsResponse.data;

                setTweets(tweetData.slice(0, MAX_TWEETS));

                if (tweetData.length > 0) {
                    const lastTweet = tweetData[tweetData.length - 1]
                    setTweetPage(lastTweet.createdAt - 1);
                }
            } catch (err) {
                if (err instanceof AxiosError && err.response) {
                    const errMessage = err.response?.data?.message ? err.response?.data?.message : "There was a problem fetching tweets.";
                    toast.error(errMessage);
                } else {
                    console.log(err);
                    toast.error("There was a problem fetching tweets.");
                }
            } finally {
                setLoading(false);
            }
        }

        init();
    }, []);

    async function loadMoreTweets(e: { preventDefault: () => void }) {
        e.preventDefault();

        try {
            setLoadingMoreTweets(true);

            const tweetsResponse = await getDailyDramaTweets(undefined, tweetPage);
            const tweetData = tweetsResponse.data;

            setTweets(tweets.concat(...tweetData.slice(0, MAX_TWEETS)));

            if (tweetData.length > 0) {
                const lastTweet = tweetData[tweetData.length - 1]
                setTweetPage(lastTweet.createdAt - 1);
            }
        } catch (err) {
            if (err instanceof AxiosError && err.response) {
                const errMessage = err.response?.data?.message ? err.response?.data?.message : "There was a problem loading more tweets.";
                toast.error(errMessage);
            } else {
                console.log(err);
                toast.error("There was a problem loading more tweets.");
            }
        } finally {
            setLoadingMoreTweets(false);
        }
    }

    function renderButton() {
        if (loadingMoreTweets) {
            return (
                <div className="w-full flex justify-center">
                    <SmallLoading />
                </div>
            );
        } else {
            return (
                <div className="w-full flex justify-center">
                    <button
                        className="w-full md:w-1/2 py-4 bg-gray-50 hover:bg-gray-100 border-gray-200 border-2 rounded-2xl text-xs text-gray-500 font-mono font-semibold uppercase hover:opacity-70 transition-opacity ease-in-out delay-50"
                        onClick={(e) => loadMoreTweets(e)}
                        disabled={loadingMoreTweets}
                    >
                        ✨ Load More Tweets ✨
                    </button>
                </div>
            );
        }
    }

    function renderTweets() {
        const tweetElements: JSX.Element[] = [];
        tweets.forEach((tweet) => {
            tweetElements.push(
                <div className="w-full">
                    <TweetCard tweet={tweet} />
                </div>
            );
        });

        return tweetElements;
    }

    function render(): JSX.Element {
        if (!loading) {
            return <Loading />;

        } else {
            return (
                <div className="w-full mb-8 flex flex-wrap justify-center md:grid md:gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {renderTweets()}
                </div>
            );
        }
    }

    return (
        <>
        <div className="container mx-auto">
            <div className="max-w-11xl mx-auto px-4 sm:px-6 lg:px-8">
                <section className="pb-24">
                    <div className="w-full pt-28 mb-6">
                        <h1 className="uppercase font-black text-2xl md:text-4xl lg:text-5xl whitespace-pre-line">The Daily Drama <span>✨</span></h1>
                    </div>

                    {render()}                    

                    {renderButton()}
                </section>
            </div>
        </div>

        <Footer footerType={2} />
        </>
    );
}

export default Drama;
