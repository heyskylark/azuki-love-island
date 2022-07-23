import { AxiosError } from "axios";
import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { getLatestSeasonsTotalVoteResults, getSeasonParticipants, getSeasonsInitialBracket } from "../clients/MainClient";
import Footer from "../components/Footer";
import Loading from "../components/Loading";
import RoundResults from "../components/vote/RoundResults";
import GenderedInitialBracket from "../models/api/GenderedInitialBracket";
import GenderedRoundWinners from "../models/api/GenderedRoundWinners";
import ParticipantResponse from "../models/api/ParticipantResponse";
import ResultsFilterState from "../models/ResultsFilterState";

function Results() {
    const [loading, setLoading] = useState<boolean>(true);
    const [filterState, setFilterState] = useState<ResultsFilterState>(ResultsFilterState.FEMALE);
    const [resultsSeason, setResultsSeason] = useState<number>(-1);
    const [participants, setParticipants] = useState<Map<String, ParticipantResponse>>(new Map());
    const [voteResults, setVoteResults] = useState<GenderedRoundWinners[]>([]);
    const [nextResultsDate, setNextResultsDate] = useState<number>(-1);

    useEffect(() => {
        async function getInitData(): Promise<void> {
            try {
                const initBracketResponse = await getSeasonsInitialBracket();
                const initBrakcet = initBracketResponse.data;
                setNextResultsDate(calculateNextVoteResults(initBrakcet))

                const latestSeasonVoteResults = await getLatestSeasonsTotalVoteResults();
                const currMaxSeason = latestSeasonVoteResults.data.seasonNumber;
                const rounds = latestSeasonVoteResults.data.rounds;

                const participantsResponse = await getSeasonParticipants(currMaxSeason);
                const participants = participantsResponse.data.participants
                setParticipants(new Map(participants.map(p => [p.id, p])));

                setResultsSeason(currMaxSeason);

                setVoteResults(rounds);
            } catch (err) {
                if (err instanceof AxiosError && err.response?.data) {
                    const data = err.response?.data;

                    toast.error(data.message);
                } else {
                    console.log("There was a problem loading results.")
                }
            } finally {
                setLoading(false);
            }
        }

        document.title = "Results / Azuki Love Island"

        getInitData();
    }, [])

    function calculateNextVoteResults(initBracket: GenderedInitialBracket): number {
        const now = Date.now();
        const startDateMilli = initBracket.voteStartDate;
        const deadilineMilli = initBracket.voteDeadline;
        const voteGapsMilli = initBracket.voteGapTimeMilliseconds;

        if (!voteGapsMilli || now >= deadilineMilli) {
            return -1;   
        }

        const firstRoundLock = startDateMilli + voteGapsMilli;
        if (now > firstRoundLock) {
            const startDelta = now - startDateMilli;
            const maxRounds = initBracket.numOfBrackets;
            const lastLockedRound = Math.min(Math.floor(startDelta / voteGapsMilli), maxRounds);

            return startDateMilli + (voteGapsMilli * (lastLockedRound + 1));
        } else {
            return firstRoundLock;
        }
    }

    function filterButtonEvent(e: React.MouseEvent<HTMLButtonElement, MouseEvent>): void {
        e.preventDefault();
        const buttonId = (e.target as HTMLButtonElement).id
		switch(buttonId) {
			case "femaleFilter": {
				setFilterState(ResultsFilterState.FEMALE);
				break;
			}
			case "maleFilter": {
				setFilterState(ResultsFilterState.MALE);
				break;
			}
			default: {
				console.log("Invalid ID");
				break;
			}
		}
    }

    function getSeasonNumber(): string {
        if (resultsSeason > 0) {
            return `${resultsSeason}`;
        } else {
            return "";
        }
    }

    function renderResults() {
        if (loading) {
            return <Loading />
        } else if (voteResults.length === 0 && nextResultsDate !== -1) {
            const date = new Date(nextResultsDate).toLocaleDateString('en-us', { year:"numeric", month:"short", day:"numeric", hour:"2-digit", minute:"2-digit" });
            
            return (
                <div className="mt-32 text-center">
                    <h1 className="mb-6 uppercase font-black text-2xl lg:text-4xl whitespace-pre-line">
                        <span className="inline-block">First Results Coming:&nbsp;</span>
                        <span className="inline-block">{date} PST</span>
                    </h1>
                </div>
            );
        } else {
            const results: JSX.Element[] = []
            voteResults.forEach(voteResult => {
                results.push(
                    <RoundResults
                        key={voteResult.roundNumber}
                        results={voteResult}
                        filterState={filterState}
                        participants={participants}
                        finalRound={5} // TODO: Pass in final round number with response
                    />
                );
            });

            return results;
        }
    }

    return (
        <>
        <div className="container mx-auto">
            <div className="max-w-11xl mx-auto px-4 sm:px-6 lg:px-8">
                <section className="pb-24">
                    <div className="w-full pt-28 pb-2">
                        <h1 className="uppercase font-black text-4xl lg:text-5xl whitespace-pre-line">Results&nbsp;<span className="opacity-10"> //</span></h1>
                        <h1 className="mb-6 uppercase font-black text-3xl lg:text-4xl whitespace-pre-line">Season: {getSeasonNumber()}</h1>
                    </div>

                    <div className="flex lg:w-7/12 mb-7">
					    <div className="w-full md:w-8/12 -ml-2 px-2 sm:px-0 py-0">
                            <div className="flex p-1 space-x-1 duration-300 bg-gray-200 lg:rounded-xl rounded-md justify-end">
                                <button
                                    id="femaleFilter"
                                    className={`w-full py-[0.3rem] lg:py-1.5 lg:text-xl text-sm leading-5 font-extrabold text-black lg:rounded-lg rounded-md focus:outline-none hover:bg-white/[0.5] duration-300 ${filterState === ResultsFilterState.FEMALE ? "bg-white" : ""}`}
                                    onClick={filterButtonEvent}
                                >
                                    GIRLZUKI
                                </button>
                                <button
                                    id="maleFilter"
                                    className={`w-full py-[0.3rem] lg:py-1.5 lg:text-xl text-sm leading-5 font-extrabold text-black lg:rounded-lg rounded-md focus:outline-none hover:bg-white/[0.5] duration-300 ${filterState === ResultsFilterState.MALE ? "bg-white" : ""}`}
                                    onClick={filterButtonEvent}
                                >
                                    BOYZUKI
                                </button>
                            </div>
                        </div>
                    </div>

                    {renderResults()}
                </section>
            </div>
        </div>

        <Footer footerType={2} />
        </>
    );
}

export default Results;
