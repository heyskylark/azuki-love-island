import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { getLatestSeasonParticipants, getLatestSeasonsTotalVoteResults } from "../clients/MainClient";
import Footer from "../components/Footer";
import Loading from "../components/Loading";
import RoundResults from "../components/vote/RoundResults";
import { useLatestSeason } from "../context/SeasonContext";
import GenderedRoundWinners from "../models/api/GenderedRoundWinners";
import ParticipantResponse from "../models/api/ParticipantResponse";
import ResultsFilterState from "../models/ResultsFilterState";

function Results() {
    const latestSeasonContext = useLatestSeason();

    const [loading, setLoading] = useState<boolean>(true);
    const [filterState, setFilterState] = useState<ResultsFilterState>(ResultsFilterState.FEMALE);
    const [participants, setParticipants] = useState<Map<String, ParticipantResponse>>(new Map());
    const [voteResults, setVoteResults] = useState<GenderedRoundWinners[]>([]);

    useEffect(() => {
        async function getInitData(): Promise<void> {
            try {
                const participantsResponse = await getLatestSeasonParticipants(null);
                setParticipants(new Map(participantsResponse.data.map(p => [p.id, p])));

                const latestSeasonVoteResults = await getLatestSeasonsTotalVoteResults();
                setVoteResults(latestSeasonVoteResults.data);
            } catch (err) {
                //@ts-ignore
                const data = err.response.data;

                toast.error(data.message);
            } finally {
                setLoading(false);
            }
        }

        getInitData();
    }, [])

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
        const latestSeasonNum = latestSeasonContext?.latestSeason?.seasonNumber;
        return latestSeasonNum ? `${latestSeasonNum}` : "";
    }

    function renderResults() {
        if (loading) {
            return <Loading />
        } else if (voteResults.length === 0) {
            const date = new Date(1655665200000).toLocaleDateString('en-us', { year:"numeric", month:"short", day:"numeric", hour:"2-digit", minute:"2-digit", second:"2-digit" });
            
            return (
                <div className="mt-32 text-center">
                    <h1 className="mb-6 uppercase font-black text-2xl lg:text-4xl whitespace-pre-line">
                        <span className="inline-block">First Results Coming:&nbsp;</span>
                        <span className="inline-block">{date}</span>
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
