import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { getLatestSeasonParticipants } from "../clients/MainClient";
import Footer from "../components/Footer";
import GalleryCard from "../components/GalleryCard";
import GalleryPreview from "../components/GalleryPreview";
import { useLatestSeason } from "../context/SeasonContext";
import ParticipantResponse from "../models/api/ParticipantResponse";
import { GalleryFilterState } from "../models/GalleryFilterState";

interface Props {
    filter: string | null;
}

function Gallery(props: Props) {
    const latestSeasonContext = useLatestSeason();

    const [loading, setLoading] = useState<boolean>(true);
    const [modalClosed, setModalClosed] = useState<boolean>(true);

    const [participants, setParticipants] = useState<ParticipantResponse[]>([]);
    const [filteredParticipants, setFilteredParticipants] = useState<ParticipantResponse[]>([]);

    const [filterState, setFilterState] = useState<GalleryFilterState>(GalleryFilterState.ALL);
    const [azukiId, setAzukiId] = useState<number>(-1);
    const [twitterHandle, setTwitterHandle] = useState<string>("");
    const [color, setColor] = useState<string>("");
    const [imageUrl, setImageUrl] = useState<string>("");
    const [bio, setBio] = useState<string>("");
    const [hobbies, setHobbies] = useState<string[]>([]);

    useEffect(() => {
        async function getParticipants() {
            try {
                console.log(props.filter)
                const participantsResponse = await getLatestSeasonParticipants(props.filter);
                const data = participantsResponse.data;

                setParticipants(data);
                setFilteredParticipants(data);
            } catch (err) {
                toast.error("There was a problem loading the participants...")
            } finally {
                setLoading(false);
            }
        }

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

    function openModal(azukId: number, twitterHandle: string, color: string, imageUrl: string, bio: string | undefined, hobbies: string[] | undefined) {
        setAzukiId(azukId);
        setTwitterHandle(twitterHandle);
        setColor(color);
        setImageUrl(imageUrl);
        setBio(bio ? bio : "");
        setHobbies(hobbies ? hobbies : [])

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
        let preview: JSX.Element[] = []
        filteredParticipants.forEach(participant => {
            preview.push(
                <GalleryPreview
                    key={participant.azukiId}
                    azukiId={participant.azukiId}
                    twitterHandle={participant.twitterHandle}
                    color={participant.backgroundTrait}
                    imageUrl={participant.imageUrl}
                    modalImageUrl={participant.imageUrl}
                    bio={participant.bio}
                    hobbies={participant.hobbies}
                    openModal={openModal}
                />
            )
        });

        return preview;
    }

    function getSeasonNumber(): string {
        const seasonNumber = latestSeasonContext?.latestSeason?.seasonNumber

        return seasonNumber ? `${seasonNumber}` : "";
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

    return (
        <>
        <div className="container mx-auto">
            <div className="max-w-11xl mx-auto px-4 sm:px-6 lg:px-8">
                <section className="pb-24">
                    <div className="w-full pt-28 pb-2">
                        <h1 className="uppercase font-black text-4xl lg:text-5xl whitespace-pre-line">Season: {getSeasonNumber()}</h1>
                        <h1 className="mb-6 uppercase font-black text-3xl lg:text-4xl whitespace-pre-line">Contestants&nbsp;<span className="opacity-10"> //</span></h1>
                    </div>

                    <div className="flex w-7/12 mb-7">
					    <div className="w-full md:w-8/12 -ml-2 px-2 sm:px-0 py-0">
                            {/* <button className="flex hover:opacity-50">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true" className="w-5 h-5">
                                        <path fillRule="evenodd" d="M3 3a1 1 0 011-1h12a1 1 0 011 1v3a1 1 0 01-.293.707L12 11.414V15a1 1 0 01-.293.707l-2 2A1 1 0 018 17v-5.586L3.293 6.707A1 1 0 013 6V3z" clipRule="evenodd" />
                                </svg>
                            </button> */}
                            <div className="flex p-1 space-x-1 duration-300 bg-gray-200 lg:rounded-xl rounded justify-end">
                                <button
                                    id="allFilter"
                                    className={`w-full lg:py-1.5 sm:py-1 sm:px-1 lg:text-xl text-xs leading-5 font-extrabold text-black lg:rounded-lg rounded-sm focus:outline-none hover:bg-white/[0.5] duration-300 ${filterState === GalleryFilterState.ALL ? "bg-white" : ""}`}
                                    onClick={filterButtonEvent}
                                >
                                    ALL
                                </button>
                                <button
                                    id="femaleFilter"
                                    className={`w-full lg:py-1.5 sm:py-1 sm:px-1 lg:text-xl text-xs leading-5 font-extrabold text-black lg:rounded-lg rounded-sm focus:outline-none hover:bg-white/[0.5] duration-300 ${filterState === GalleryFilterState.FEMALE ? "bg-white" : ""}`}
                                    onClick={filterButtonEvent}
                                >
                                    GIRLZUKI
                                </button>
                                <button
                                    id="maleFilter"
                                    className={`w-full lg:py-1.5 sm:py-1 sm:px-1 lg:text-xl text-xs leading-5 font-extrabold text-black lg:rounded-lg rounded-sm focus:outline-none hover:bg-white/[0.5] duration-300 ${filterState === GalleryFilterState.MALE ? "bg-white" : ""}`}
                                    onClick={filterButtonEvent}
                                >
                                    BOYZUKI
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="flex">
                        <div className="w-full">
                            <div className="grid lg:grid-cols-5 2xl:grid-cols-5 grid-cols-2 lg:gap-x-6 gap-x-4 lg:gap-y-2 gap-y-1 lg:col-span-3">
                                {renderParticipants()}
                            </div>
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
