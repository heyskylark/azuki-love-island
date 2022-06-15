import { useEffect, useState } from "react";
import { getLatestSeason, getLatestSeasonParticipantCount } from "../clients/MainClient";
import Footer from "../components/Footer";
import SubmissionForm from "../components/SubmissionForm";

function Submission() {
    const MAX_PARTICIPANT_NUM_LENGTH = 4

    const [loading, setLoading] = useState<boolean>(true);
    const [submissionClosed, setSubmissionClosed] = useState<boolean>(false);
    const [participantCount, setParticipantCount] = useState<number>(-1);
    const [azukiId, setAzukiId] = useState<string>("");
    const [twitterHandle, setTwitterHandle] = useState<string>("");
    const [bio, setBio] = useState<string>("");
    const [hobbies, setHobbies] = useState<string>("");

    const [loadingSubmission, setLoadingSubmission] = useState<boolean>(false);

    useEffect(() => {
        async function fetchInitData() {
            setLoading(true);
            
            try {
                const latestSeasonResponse = await getLatestSeason();
                if (!latestSeasonResponse.data.submissionActive) {
                    setSubmissionClosed(true);
                }

                const countResponse = await getLatestSeasonParticipantCount();
                setParticipantCount(countResponse.data.count);
            } catch (err) {
                // TODO: do we put up a toast?
            } finally {
                setLoading(false);
            }
        }
        
        fetchInitData();
    }, []);

    function getParticipantCount(count: number): string {
        if (count > -1) {
            var numString = "0000" + count;
            return numString.substring(numString.length - MAX_PARTICIPANT_NUM_LENGTH);
        } else {
            return "----";
        }
    }

    function renderClosed() {
        if (submissionClosed) {
            return (
                <div className="lg:w-1/2 pr-20 absolute z-50">
                    <img src="images/closed.png" alt="" />
                </div>
            );
        }
    }

    return (
        <>
        <div className='container mx-auto'>
            <div className="duration-300 min-h-screen bg-white">
                <main className="max-w-11xl mx-auto mb-8 lg:mb-20 px-4 sm:px-6 lg:px-8">
                    <div className="w-full pt-28">
                        {renderClosed()}
                        <div className="flex">
                            <div className="lg:w-1/2 lg:pr-4 lg:pb-0">
                                <h1 className="uppercase font-black text-4xl lg:text-5xl whitespace-pre-line">Azuki Love Island</h1>
                                <h1 className="mb-6 uppercase font-black text-3xl lg:text-4xl whitespace-pre-line">
                                    Submissions: {getParticipantCount(participantCount)}<span className="opacity-10"> //</span>
                                </h1>
                                <p className="mb-6 font-mono text-gray-800 lg:text-sm text-xs lg:leading-6 leading-4">
                                    64 Azuki battle it out in a heated tournament consisting
                                    of 32 match-ups voted on by the community. Who will become
                                    the King and Queen of the Island? Let's find out.<br/>
                                    <br/>
                                    Tell us about yourself. What is your Azuki's story? 
                                    Some say personality is more important than looks. 
                                    Imagine having both.<br/>
                                    <br/>
                                    Good luck.
                                </p>
                                <SubmissionForm
                                    azukiId={azukiId}
                                    twitterHandle={twitterHandle}
                                    bio={bio}
                                    hobbies={hobbies}
                                    loadingPage={loading}
                                    loadingSubmission={loadingSubmission}
                                    submissionsClosed={submissionClosed}
                                    setAzukiId={setAzukiId}
                                    setTwitterHandle={setTwitterHandle}
                                    setBio={setBio}
                                    setHobbies={setHobbies}
                                    setLoadingSubmission={setLoadingSubmission}
                                />
                            </div>
                            <div className="lg:w-1/2 hidden lg:block z-49">
                                <img className="fixed bottom-0 right-0 w-1/2" src="images/sexy-beanz-2.png" alt="Love Island Beanz" />
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <Footer footerType={1} />
        </>
    )
}

export default Submission;
