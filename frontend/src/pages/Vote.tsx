import { createRef, useEffect, useState } from "react";
import Footer from "../components/Footer";
import Loading from "../components/Loading";
import TwitterHandleField from "../components/vote/TwitterHandleField";
import VoteFinished from "../components/vote/VoteFinished";
import VoteWindow from "../components/vote/VoteWindow";
import Countdown from 'react-countdown';
import useVote from "../hooks/useVote";
import { VoteStateEnum } from "../models/hooks/UseVoteModel";

function Vote() {
    const countDownRef = createRef<Countdown>();

    const [joined, setJoined] = useState<boolean>(false);
    // TODO: need to setup state management where can check if user started voting but never finished the round
    // useEffect(() => {
    //     return function voteCheck() {
    //         if (joined)  {
    //             console.log("Mic check...", voteState);
    //         } else {
    //             setJoined(true);
    //         }
    //     }
    // }, [joined])

    const {
        state,
        seasonNumber,
        currentRoundNumber,
        finalRoundNumber,
        startDate,
        remainingVotes,
        undoDisabled,
        currentVoteGroup,
        nextRoundDate,
        undo,
        vote,
        registerTwitterHandle
    } = useVote();

    useEffect(() => {
        document.title = "Vote / Azuki Love Island";
    }, [])

    useEffect(() => {
        // TODO: Need to add a trigger to refresh vote states once timer runs out
        if (nextRoundDate()) {
            countDownRef.current?.start();
        }
    }, [nextRoundDate, countDownRef])

    function renderVoteHeader(): JSX.Element {
        let header: JSX.Element
        if (state === VoteStateEnum.VOTING_NOT_STARTED) {
            const date = new Date(startDate).toLocaleDateString('en-us', { year:"numeric", month:"short", day:"numeric", hour:"2-digit", minute:"2-digit" });
            header = (
                <>
                <h1 className="uppercase font-black text-xl lg:text-4xl whitespace-pre-line">Voting Starts:</h1>
                <h1 className="mb-6 uppercase font-black text-xl lg:text-4xl whitespace-pre-line">{date} PST</h1>
                </>
            )
        } else {
            const nextRound = nextRoundDate() ? nextRoundDate() : new Date();
            header = (
                <>
                <h1 className="uppercase font-black text-4xl lg:text-5xl whitespace-pre-line">Round: {currentRoundNumber} / {finalRoundNumber}</h1>
                <h1 className="uppercase font-black text-xl lg:text-4xl whitespace-pre-line">Votes Left For Round: {remainingVotes}</h1>
                <h1 className="mb-6 uppercase font-black text-xl lg:text-4xl whitespace-pre-line">Round Ends At: <Countdown ref={countDownRef} date={nextRound} /></h1>
                </>
            )
        }

        return header
    }

    function renderVoteSection(): JSX.Element {
        if (state === VoteStateEnum.LOADING) {
            return <Loading />;
        }

        if (state === VoteStateEnum.VOTING_ENDED) {
            return <VoteFinished text={`Season ${seasonNumber} voting has ended`} />;
        } else if (state === VoteStateEnum.VOTING_NOT_STARTED) {
            return <VoteFinished text={""} />;
        } else if (state === VoteStateEnum.FINISHED_VOTING) {
            const nextRound = nextRoundDate();

            if (nextRound) {
                const dateString = nextRound.toLocaleDateString('en-us', { year:"numeric", month:"short", day:"numeric", hour:"2-digit", minute:"2-digit" });
                return <VoteFinished text={`Next Round Starts:\n${dateString} PST`} />;
            } else {
                return <VoteFinished text="Thanks For Voting!" />;
            }
        } else if (state === VoteStateEnum.REGISTER) {
            return <TwitterHandleField
                registerTwitterHandle={registerTwitterHandle}
            />
        }

        const nextVote = currentVoteGroup;

        if (nextVote?.submission1 && nextVote.submission2) {
            return (
                <div className="w-full flex flex-wrap justify-center">
                    <div className="lg:w-3/4">
                        <VoteWindow
                            participant1={nextVote.submission1}
                            participant2={nextVote.submission2}
                            addVote={vote}
                        />
                    </div>
                </div>
            );
        }

        return <Loading />;
    }

    function renderVotePageContent(): JSX.Element {
        if (state === VoteStateEnum.LOADING) {
            return <Loading />
        } else {
            return (
                <>
                <div className="w-full flex flex-wrap justify-center lg:text-center pt-28">
                    <div className="w-full lg:w-1/2">
                        {renderVoteHeader()}

                        {state !== VoteStateEnum.REGISTER ? <button
                            className="transition-opacity ease-in-out delay-50 disabled:opacity-70 uppercase mb-8 w-full p-3 rounded-md text-white bg-azukired whitespace-nowrap hover:opacity-70"
                            type="submit"
                            onClick={() => undo()}
                            disabled={undoDisabled}
                        >
                            Undo
                        </button> : <></>}
                    </div>
                </div>

                {renderVoteSection()}
                </>
            )
        }
    }

    return (
        <>
        <div className="container mx-auto">
            <div className="w-full mx-auto px-4 sm:px-6 lg:px-8">
                <section className="pb-24">
                    {renderVotePageContent()}
                </section>
            </div>
        </div>

        <Footer footerType={2} />
        </>
    );
}

export default Vote;
