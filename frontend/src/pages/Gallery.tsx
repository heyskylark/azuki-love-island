import ReactGA from "react-ga4";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "react-toastify";
import { getLatestSeasonParticipants, getLatestSeasonsTotalVoteResults, getSeasonsInitialBracket as getLatestInitialBracket } from "../clients/MainClient";
import Footer from "../components/Footer";
import GalleryCard from "../components/GalleryCard";
import GalleryPreview from "../components/GalleryPreview";
import Loading from "../components/Loading";
import GenderedRoundWinners from "../models/api/GenderedRoundWinners";
import ParticipantResponse from "../models/api/ParticipantResponse";
import { GalleryFilterState } from "../models/GalleryFilterState";

function Gallery() {
    const [loading, setLoading] = useState<boolean>(true);
    const [votingOpen, setVotingOpen] = useState<boolean>(false);
    const [modalClosed, setModalClosed] = useState<boolean>(true);

    const [maxSeason, setMaxSeason] = useState<number>(-1);
    const [gallerySeason, setGallerySeason] = useState<number>(-1);
    const [participants, setParticipants] = useState<ParticipantResponse[]>([]);
    const [filteredParticipants, setFilteredParticipants] = useState<ParticipantResponse[]>([]);
    const [voteResults, setVoteResults] = useState<GenderedRoundWinners[]>([]);

    const [filterState, setFilterState] = useState<GalleryFilterState>(GalleryFilterState.ALL);
    const [azukiId, setAzukiId] = useState<number>(-1);
    const [twitterHandle, setTwitterHandle] = useState<string>("");
    const [color, setColor] = useState<string>("");
    const [imageUrl, setImageUrl] = useState<string>("");
    const [bio, setBio] = useState<string>("");
    const [hobbies, setHobbies] = useState<string[]>([]);

    useEffect(() => {
        function getParticipants() {
            getLatestSeasonParticipants()
                .then((participantsResponse) => participantsResponse.data)
                .then((data) => {
                    const participants = data.participants
                    const sortedParticipants = participants.sort((p1: ParticipantResponse, p2: ParticipantResponse) => {
                        return p1.azukiId - p2.azukiId
                    });
    
                    setMaxSeason(data.seasonNumber);
                    setGallerySeason(data.seasonNumber);

                    setParticipants(sortedParticipants);
                    setFilteredParticipants(sortedParticipants);
                })
                .catch((err) => {
                    toast.error("There was a problem loading the participants...");
                })
                .finally(() => {
                    setLoading(false);
                })

            // TODO: Re-think this, banners about voting and results should only show up for the latest gallery. Fetch initialBracket and voteResults based on maxSeason
            getLatestInitialBracket()
                .then((initialBracketResponse) => initialBracketResponse.data)
                .then(async (initialBracketData) => {
                    const voteStartDate = new Date(initialBracketData.voteStartDate).getTime();
                    const voteEndDate = new Date(initialBracketData.voteDeadline).getTime();
                    const now = new Date().getTime();
                    if (now >= voteStartDate && now <= voteEndDate) {
                        setVotingOpen(true);
                    }

                    const latestSeasonVoteResults = await getLatestSeasonsTotalVoteResults();
                    setVoteResults(latestSeasonVoteResults.data.rounds);
                })
        }

        document.title = "Gallery / Azuki Love Island"

        getParticipants();
    }, []);

    useEffect(() => {
        if (filterState === GalleryFilterState.FEMALE) {
            const filtered = participants.filter(participant => {
                return participant.gender === "FEMALE"
            });

            setFilteredParticipants(filtered);
        } else if (filterState === GalleryFilterState.MALE) {
            const filtered = participants.filter(participant => {
                return participant.gender === "MALE"
            });

            setFilteredParticipants(filtered);
        } else {
            setFilteredParticipants(participants);
        }
    }, [participants, filterState]);

    function viewProfileEvent(azukiId: number) {
        ReactGA.event({
            category: "engagement",
            action: "view_gallery_bio",
            value: azukiId,
        });
    }

    function openModal(azukId: number, twitterHandle: string, color: string, imageUrl: string, bio: string | undefined, hobbies: string[] | undefined) {
        setAzukiId(azukId);
        setTwitterHandle(twitterHandle);
        setColor(color);
        setImageUrl(imageUrl);
        setBio(bio ? bio : "");
        setHobbies(hobbies ? hobbies : []);

        viewProfileEvent(azukId);

        setModalClosed(false);
    }

    function closeModal() {
        setAzukiId(-1);
        setTwitterHandle("");
        setColor("");
        setImageUrl("");
        setBio("");
        setHobbies([])

        setModalClosed(true);
    }

    function renderModal() {
        if (!modalClosed) {
            return (
                <GalleryCard
                    azukiId={azukiId}
                    twitterHandle={twitterHandle}
                    color={color}
                    imageUrl={imageUrl}
                    bio={bio}
                    hobbies={hobbies}
                    closeModal={closeModal}
                />
            );
        }
    }

    function renderParticipants() {
        if (loading) {
            return <Loading />
        } else {
            let preview: JSX.Element[] = []
            filteredParticipants.forEach(participant => {
                preview.push(
                    <GalleryPreview
                        key={participant.azukiId}
                        azukiId={participant.azukiId}
                        twitterHandle={participant.twitterHandle}
                        color={participant.backgroundTrait}
                        modalImageUrl={participant.imageUrl}
                        bio={participant.bio}
                        hobbies={participant.hobbies}
                        openModal={openModal}
                    />
                )
            });

            return (
                <div className="grid lg:grid-cols-5 2xl:grid-cols-5 grid-cols-2 lg:gap-x-6 gap-x-4 lg:gap-y-2 gap-y-1 lg:col-span-3">
                    {preview}
                </div>
            );
        }
    }

    function getSeasonNumber(): string {
        if (gallerySeason > 0) {
            return `${gallerySeason}`
        } else {
            return ""   
        }
    }

    function filterButtonEvent(e: React.MouseEvent<HTMLButtonElement, MouseEvent>): void {
        e.preventDefault();
        const buttonId = (e.target as HTMLButtonElement).id
		switch(buttonId) {
			case "allFilter": {
				setFilterState(GalleryFilterState.ALL);
				break;
			}
			case "femaleFilter": {
				setFilterState(GalleryFilterState.FEMALE);
				break;
			}
			case "maleFilter": {
				setFilterState(GalleryFilterState.MALE);
				break;
			}
			default: {
				console.log("Invalid ID");
				break;
			}
		}
    }

    function renderVoting(): JSX.Element | undefined {
        // TODO: Render this banner when only latest season voting exists and is open
        if (votingOpen) {
            return (
                <div className="flex w-full content-center mb-7">
                    <h1 className="pt-1 lg:pt-0.5 mr-2 uppercase font-black text-md md:text-xl lg:text-2xl whitespace-pre-line">Season {maxSeason} Voting is now Open:&nbsp;</h1>
                    <Link className="uppercase font-semibold text-xs hover:opacity-60 duration-300 py-3 px-4 rounded bg-gray-200" to="/vote">
                        Vote Now →
                    </Link>
                </div>
            );
        }
    }

    function renderResults(): JSX.Element | undefined {
        // TODO: Render this banner when only latest season results exists
        if (!votingOpen && voteResults.length > 0) {
            return (
                <div className="flex w-full content-center mb-7">
                    <h1 className="pt-1 lg:pt-0.5 mr-2 uppercase font-black text-md md:text-xl lg:text-2xl whitespace-pre-line">Season {maxSeason} Results are Out:&nbsp;</h1>
                    <Link className="uppercase font-semibold text-xs hover:opacity-60 duration-300 py-3 px-4 rounded bg-gray-200" to="/results">
                        Results →
                    </Link>
                </div> 
            );
        }
    }

    return (
        <>
        <div className="container mx-auto">
            <div className="max-w-11xl mx-auto px-4 sm:px-6 lg:px-8">
                <section className="pb-24">
                    <div className="w-full pt-28 pb-2">
                        <h1 className="uppercase font-black text-4xl lg:text-5xl whitespace-pre-line">Season: {getSeasonNumber()}</h1>
                        <h1 className="mb-6 uppercase font-black text-3xl lg:text-4xl whitespace-pre-line">Contestants&nbsp;<span className="opacity-10"> //</span></h1>
                    </div>

                    <div className="flex lg:w-7/12 mb-7">
					    <div className="w-full md:w-8/12 -ml-2 px-2 sm:px-0 py-0">
                            <div className="flex p-1 space-x-1 duration-300 bg-gray-200 lg:rounded-xl rounded justify-end">
                                <button
                                    id="allFilter"
                                    className={`w-full py-[0.3rem] lg:py-1.5 sm:px-1 lg:text-xl text-xs leading-5 font-extrabold text-black lg:rounded-lg rounded-sm focus:outline-none hover:bg-white/[0.5] duration-300 ${filterState === GalleryFilterState.ALL ? "bg-white" : ""}`}
                                    onClick={filterButtonEvent}
                                >
                                    ALL
                                </button>
                                <button
                                    id="femaleFilter"
                                    className={`w-full py-[0.3rem] lg:py-1.5 lg:text-xl text-xs leading-5 font-extrabold text-black lg:rounded-lg rounded-sm focus:outline-none hover:bg-white/[0.5] duration-300 ${filterState === GalleryFilterState.FEMALE ? "bg-white" : ""}`}
                                    onClick={filterButtonEvent}
                                >
                                    GIRLZUKI
                                </button>
                                <button
                                    id="maleFilter"
                                    className={`w-full py-[0.3rem] lg:py-1.5 lg:text-xl text-xs leading-5 font-extrabold text-black lg:rounded-lg rounded-sm focus:outline-none hover:bg-white/[0.5] duration-300 ${filterState === GalleryFilterState.MALE ? "bg-white" : ""}`}
                                    onClick={filterButtonEvent}
                                >
                                    BOYZUKI
                                </button>
                            </div>
                        </div>
                    </div>

                    {renderResults()}
                    {renderVoting()}

                    <div className="flex">
                        <div className="w-full">
                            {renderParticipants()}
                        </div>
                    </div>
                </section>
            </div>
        </div>

        {renderModal()}

        <Footer footerType={2} />
        </>
    );
}

export default Gallery;
