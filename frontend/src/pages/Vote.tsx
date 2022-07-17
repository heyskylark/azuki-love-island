import { createRef, useEffect, useState } from "react";
import { toast } from "react-toastify";
import { getLatestSeasonParticipants, getLatestVoteBracket, getSeasonsInitialBracket, voteOnGenderedBracket } from "../clients/MainClient";
import Footer from "../components/Footer";
import Loading from "../components/Loading";
import TwitterHandleField from "../components/vote/TwitterHandleField";
import VoteFinished from "../components/vote/VoteFinished";
import VoteWindow from "../components/vote/VoteWindow";
import BracketGroup, { BracketGroupCreator } from "../models/api/BracketGroup";
import ParticipantResponse from "../models/api/ParticipantResponse";
import VoteRequest from "../models/api/VoteRequest";
import Countdown from 'react-countdown';

function Vote() {
    const countDownRef = createRef<Countdown>();

    const [seasonNumber, setSeasonNumber] = useState<string>("");

    const [voteState, setVoteState] = useState<string>("INIT");
    const [twitterHandleSubmited, setTwitterHandleSubmitted] = useState<boolean>(false);
    const [twitterHandle, setTwitterHandle] = useState<string>("");

    const [participants, setParticipants] = useState<Map<string, ParticipantResponse>>(new Map());

    const [maleVoteIndex, setMaleVoteIndex] = useState<number>(0);
    const [femaleVoteIndex, setFemaleVoteIndex] = useState<number>(0);

    const [recentFemaleBracket, setRecentFemaleBracket] = useState<BracketGroup[]>([]);
    const [recentMaleBracket, setRecentMaleBracket] = useState<BracketGroup[]>([]);

    const [newBracket, setNewBracket] = useState<BracketGroupCreator>({})
    const [newFemaleBracket, setNewFemaleBracket] = useState<BracketGroup[]>([]);
    const [newMaleBracket, setNewMaleBracket] = useState<BracketGroup[]>([]);

    const [currentBracketNumber, setCurrentBracketNumber] = useState<number>(0);
    const [finalBracketNumber, setFinalBracketNumber] = useState<number>(-1);

    const [voteClosed, setVoteClosed] = useState<boolean>(false);
    const [voteStartDate, setVoteStartDate] = useState<Date>(new Date());
    const [voteEndDate, setVoteEndDate] = useState<number>(10);

    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        async function initData() {
            try {
                const participantsResponse = await getLatestSeasonParticipants();
                const data = participantsResponse.data;
                const participants = data.participants;

                const tempMap = new Map()
                participants.forEach(participant => {
                    tempMap.set(participant.id, participant);
                });
                setParticipants(tempMap);

                const initialBracketResponse = await getSeasonsInitialBracket();
                const initialBracketData = initialBracketResponse.data;

                const now = new Date().getTime();
                const voteStartDate = new Date(initialBracketData.voteStartDate);
                const voteEndDate = new Date(initialBracketData.voteDeadline);

                setVoteStartDate(voteStartDate);
                setVoteEndDate(voteEndDate.getTime());
                setSeasonNumber(`${initialBracketData.seasonNumber}`);

                if (now < voteStartDate.getTime() || now > voteEndDate.getTime()) {
                    setVoteClosed(true);
                }

                setFinalBracketNumber(initialBracketData.numOfBrackets);

                try {
                    const latestVoteResponse = await getLatestVoteBracket();
                    const latestVoteData = latestVoteResponse.data;

                    setTwitterHandle(latestVoteData.twitterHandle)
                    setTwitterHandleSubmitted(true);
                    
                    setCurrentBracketNumber(latestVoteData.bracketNumber);
    
                    setRecentFemaleBracket(
                        latestVoteData.femaleBracketGroups.sort((b1: BracketGroup, b2: BracketGroup) => {
                            return b1.sortOrder - b2.sortOrder
                        })
                    );
    
                    setRecentMaleBracket(
                        latestVoteData.maleBracketGroups.sort((b1: BracketGroup, b2: BracketGroup) => {
                            return b1.sortOrder - b2.sortOrder
                        })
                    );
                } catch (err) {
                    //@ts-ignore
                    const status = err.response.status
    
                    if (status !== 404) {
                        toast.error("There was a problem contacting the server.")
                    } else {
                        setRecentFemaleBracket(
                            initialBracketData.femaleBracketGroups.sort((b1: BracketGroup, b2: BracketGroup) => {
                                return b1.sortOrder - b2.sortOrder
                            })
                        );
                        
                        setRecentMaleBracket(
                            initialBracketData.maleBracketGroups.sort((b1: BracketGroup, b2: BracketGroup) => {
                                return b1.sortOrder - b2.sortOrder
                            })
                        );
                    }
                } finally {
                    setLoading(false);
                }
            } catch(err) {
                toast.error("There was a problem contacting the server.")
            }
        }

        initData();
    }, [])

    useEffect(() => {
        countDownRef.current?.start();
    }, [voteEndDate, countDownRef])

    useEffect(() => {
        if (currentBracketNumber === finalBracketNumber || voteClosed) {
            setVoteState("DONE");
        } else if (twitterHandle.length !== 0 && twitterHandleSubmited) {
            setVoteState("FEMALE");
        }
    }, [currentBracketNumber, finalBracketNumber, twitterHandle, twitterHandleSubmited, voteClosed])

    // We need to generate the next bracket group by iterating through the groups for male and female

    function getRemainingBracketVotes(): number {
        if (voteState === "DONE") {
            return 0;
        } else {
            return (recentFemaleBracket.length - femaleVoteIndex) + (recentMaleBracket.length - maleVoteIndex);
        }
    }

    async function sendVoteAndSetupNewBracket(maleBracketGroup: BracketGroup[]) {
        setLoading(true);

        try {
            const voteRequest: VoteRequest = {
                twitterHandle: twitterHandle,
                femaleBracketGroups: newFemaleBracket,
                maleBracketGroups: maleBracketGroup
            }
            const voteResponse = await voteOnGenderedBracket(voteRequest);
            const voteData = voteResponse.data

            setCurrentBracketNumber(voteData.bracketNumber);

            setFemaleVoteIndex(0);
            setRecentFemaleBracket(
                voteData.femaleBracketGroups.sort((b1: BracketGroup, b2: BracketGroup) => {
                    return b1.sortOrder - b2.sortOrder
                })
            );
            setNewFemaleBracket([]);

            setMaleVoteIndex(0);
            setRecentMaleBracket(
                voteData.maleBracketGroups.sort((b1: BracketGroup, b2: BracketGroup) => {
                    return b1.sortOrder - b2.sortOrder
                })
            );
            setNewMaleBracket([]);

            setNewBracket({});
        } catch (err) {
            //@ts-ignore
            const data = err.response.data;

            toast.error(data.message);
        } finally {
            setLoading(false);
        }
    }

    function addVote(participantId: string): void {
        if (voteState !== "DONE") {
            // This is for if we reach the final bracket where only ONE Azuki is choosen.
            // Probably a cleaner way to do this in the future
            if ((currentBracketNumber + 1 === finalBracketNumber) && recentFemaleBracket?.length === 1) {
                if (voteState === "FEMALE") {
                    const bracket = {
                        submissionId1: participantId,
                        sortOrder: newFemaleBracket.length
                    }

                    setNewFemaleBracket(oldBracket => [...oldBracket, bracket]);

                    setNewBracket({});

                    setVoteState("MALE");
                } else if (voteState === "MALE") {
                    const bracket = {
                        submissionId1: participantId,
                        sortOrder: newMaleBracket.length
                    }

                    setNewBracket({});

                    if (currentBracketNumber === finalBracketNumber) {
                        setVoteState("DONE");
                    } else {
                        sendVoteAndSetupNewBracket([...newMaleBracket, bracket]);
                        return;
                    }
                }
            }

            if (!newBracket.submissionId1) {
                setNewBracket({ submissionId1: participantId });

                if (voteState === "FEMALE") {
                    setFemaleVoteIndex(femaleVoteIndex + 1);
                } else if (voteState === "MALE") {
                    setMaleVoteIndex(maleVoteIndex + 1);
                }
            } else if (!newBracket.submissionId2) {
                if (voteState === "FEMALE") {
                    setFemaleVoteIndex(femaleVoteIndex + 1);

                    if (femaleVoteIndex + 1 === recentFemaleBracket.length) {
                        const bracket = {
                            submissionId1: newBracket.submissionId1,
                            submissionId2: participantId,
                            sortOrder: newFemaleBracket.length
                        }
    
                        setNewFemaleBracket(oldBracket => [...oldBracket, bracket]);

                        setNewBracket({});

                        setVoteState("MALE");
                    } else {
                        setNewBracket({...newBracket, submissionId2: participantId});
                    }
                } else if (voteState === "MALE") {
                    setMaleVoteIndex(maleVoteIndex + 1);

                    if (maleVoteIndex + 1 === recentMaleBracket.length) {
                        const bracket = {
                            submissionId1: newBracket.submissionId1,
                            submissionId2: participantId,
                            sortOrder: newMaleBracket.length
                        }

                        setNewBracket({});

                        if (currentBracketNumber === finalBracketNumber) {
                            setVoteState("DONE");
                        } else {
                            sendVoteAndSetupNewBracket([...newMaleBracket, bracket]);
                        }
                    } else {
                        setNewBracket({...newBracket, submissionId2: participantId});
                    }
                }
            } else {
                if (voteState === "FEMALE") {
                    const bracket = {
                        submissionId1: newBracket.submissionId1,
                        submissionId2: newBracket.submissionId2,
                        sortOrder: newFemaleBracket.length
                    }

                    setNewFemaleBracket(oldBracket => [...oldBracket, bracket]);

                    setFemaleVoteIndex(femaleVoteIndex + 1);
                } else {
                    const bracket = {
                        submissionId1: newBracket.submissionId1,
                        submissionId2: newBracket.submissionId2,
                        sortOrder: newMaleBracket.length
                    }

                    setNewMaleBracket(oldBracket => [...oldBracket, bracket]);

                    setMaleVoteIndex(maleVoteIndex + 1);
                }

                setNewBracket({submissionId1: participantId});
            }
        }
    }

    function getCurrentVoteGroup(): BracketGroup | undefined {
        if (voteState === "FEMALE") {
            return recentFemaleBracket[femaleVoteIndex];
        } else if (voteState === "MALE") {
            return recentMaleBracket[maleVoteIndex];
        }
    }

    function renderVoteSection(): JSX.Element {
        if (loading) {
            return <Loading />;
        }

        if (voteClosed) {
            const now = new Date().getTime();
            const text = now < voteStartDate.getTime() ? `Voting has not begun for Season ${seasonNumber}` : `Voting has ended for Season ${seasonNumber}`

            return <VoteFinished text={text} />;
        } else if (voteState === "DONE") {
            return <VoteFinished text="Thanks For Voting!" />;
        } else if (voteState === "INIT") {
            return <TwitterHandleField
                twitterHandle={twitterHandle}
                setTwitterHandle={setTwitterHandle}
                setTwitterHandleSubmitted={setTwitterHandleSubmitted}
            />
        }

        const nextVote = getCurrentVoteGroup();

        if (nextVote && nextVote.submissionId2) {
            const sub1 = participants.get(nextVote.submissionId1);
            const sub2 = participants.get(nextVote.submissionId2);

            if (sub1 && sub2) {
                return (
                    <div className="w-full flex flex-wrap justify-center">
                        <div className="lg:w-3/4">
                            <VoteWindow
                                participant1={sub1}
                                participant2={sub2}
                                addVote={addVote}
                            />
                        </div>
                    </div>
                );
            }
        }

        return <Loading />;
    }

    function undo(e: React.MouseEvent<HTMLButtonElement, MouseEvent>): void {
        e.preventDefault();

        if (disableUndo()) {
            return;
        }

        if (((currentBracketNumber + 1 === finalBracketNumber) && recentFemaleBracket?.length === 1)) {
            if (voteState === "MALE") {
                setNewBracket({});

                setVoteState("FEMALE");

                setNewFemaleBracket([]);

                setFemaleVoteIndex(femaleVoteIndex - 1);
            }

            return;
        }

        if (voteState === "FEMALE" && (femaleVoteIndex > 0 || newBracket.submissionId1 || newBracket.submissionId2)) {
            if (newBracket.submissionId2) {
                setNewBracket({submissionId1: newBracket.submissionId1});
                
                setFemaleVoteIndex(femaleVoteIndex - 1);
            } else if (newBracket.submissionId1) {
                const prev = newFemaleBracket.pop()

                if (prev) {
                    setNewBracket({submissionId1: prev?.submissionId1, submissionId2: prev?.submissionId2});
                } else {
                    setNewBracket({});
                }

                setNewFemaleBracket(newFemaleBracket.slice(0, newFemaleBracket.length));

                setFemaleVoteIndex(femaleVoteIndex - 1);
            }
        } else if (voteState === "MALE") {
            if (newBracket.submissionId2) {
                setNewBracket({submissionId1: newBracket.submissionId1});

                setMaleVoteIndex(maleVoteIndex - 1);
            } else if (newBracket.submissionId1) {
                const prev = newMaleBracket.pop()

                if (prev) {
                    setNewBracket({submissionId1: prev?.submissionId1, submissionId2: prev?.submissionId2})
                } else {
                    setNewBracket({});
                }

                setNewMaleBracket(newMaleBracket.slice(0, newMaleBracket.length));

                setMaleVoteIndex(maleVoteIndex - 1);
            } else {
                const prev = newFemaleBracket.pop()

                setNewBracket({submissionId1: prev?.submissionId1});

                setVoteState("FEMALE");

                setNewFemaleBracket(newFemaleBracket.slice(0, newFemaleBracket.length));

                setFemaleVoteIndex(femaleVoteIndex - 1);
            }
        }
    }

    function disableUndo(): boolean {
        if (voteState === "FEMALE") {
            return newBracket.submissionId1 == null && newFemaleBracket.length === 0;
        } else if (voteState === "MALE") {
            return false;
        }

        return true;
    }

    return (
        <>
        <div className="container mx-auto">
            <div className="w-full mx-auto px-4 sm:px-6 lg:px-8">
                <section className="pb-24">
                    <div className="w-full flex flex-wrap justify-center lg:text-center pt-28">
                        <div className="w-full lg:w-1/2">
                            <h1 className="uppercase font-black text-4xl lg:text-5xl whitespace-pre-line">Round: {currentBracketNumber} / {finalBracketNumber !== -1 ? finalBracketNumber : 0}</h1>
                            <h1 className="uppercase font-black text-xl lg:text-4xl whitespace-pre-line">Votes Left For Round: {getRemainingBracketVotes()}</h1>
                            <h1 className="mb-6 uppercase font-black text-xl lg:text-4xl whitespace-pre-line">Votes Ends At: <Countdown ref={countDownRef} date={voteEndDate} /></h1>

                            {twitterHandleSubmited ? <button
                                className="transition-opacity ease-in-out delay-50 disabled:opacity-70 uppercase mb-8 w-full p-3 rounded-md text-white bg-azukired whitespace-nowrap hover:opacity-70"
                                type="submit"
                                onClick={undo}
                                disabled={disableUndo()}
                            >
                                Undo
                            </button> : <></>}
                        </div>
                    </div>

                    {renderVoteSection()}
                </section>
            </div>
        </div>

        <Footer footerType={2} />
        </>
    );
}

export default Vote;
